/*
 * Copyright (C) 2014 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package okio;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

import static okio.Util.checkOffsetAndCount;

/**
 * This timeout uses a background thread to take action exactly when the timeout occurs. Use this to
 * implement timeouts where they aren't supported natively, such as to sockets that are blocked on
 * writing.
 *
 * <p>Subclasses should override {@link #timedOut} to take action when a timeout occurs. This method
 * will be invoked by the shared watchdog thread so it should not do any long-running operations.
 * Otherwise we risk starving other timeouts from being triggered.
 *
 * <p>Use {@link #sink} and {@link #source} to apply this timeout to a stream. The returned value
 * will apply the timeout to each operation on the wrapped stream.
 *
 * <p>Callers should call {@link #enter} before doing work that is subject to timeouts, and {@link
 * #exit} afterwards. The return value of {@link #exit} indicates whether a timeout was triggered.
 * Note that the call to {@link #timedOut} is asynchronous, and may be called after {@link #exit}.
 */
public class AsyncTimeout extends Timeout {
  /**
   * Don't write more than 64 KiB of data at a time, give or take a segment. Otherwise slow
   * connections may suffer timeouts even when they're making (slow) progress. Without this, writing
   * a single 1 MiB buffer may never succeed on a sufficiently slow connection.
   */
  private static final int TIMEOUT_WRITE_SIZE = 64 * 1024;

  /** Duration for the watchdog thread to be idle before it shuts itself down. watchdog线程关闭前空闲时间 */
  private static final long IDLE_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(60);
  private static final long IDLE_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(IDLE_TIMEOUT_MILLIS);

  /**
   * The watchdog thread processes a linked list of pending timeouts, sorted in the order to be
   * triggered. This class synchronizes on AsyncTimeout.class. This lock guards the queue.
   *
   * <p>Head's 'next' points to the first element of the linked list. The first element is the next
   * node to time out, or null if the queue is empty. The head is null until the watchdog thread is
   * started and also after being idle for {@link #IDLE_TIMEOUT_MILLIS}.
   */
  static @Nullable AsyncTimeout head; // 超时头结点

  /** True if this node is currently in the queue. */
  private boolean inQueue; // 该结点在队列中

  /** The next node in the linked list. */
  private @Nullable AsyncTimeout next; // 下一个超时结点

  /** If scheduled, this is the time that the watchdog should time this out. */
  private long timeoutAt; // 超时点

  public final void enter() {
    if (inQueue) throw new IllegalStateException("Unbalanced enter/exit");
    long timeoutNanos = timeoutNanos();
    boolean hasDeadline = hasDeadline();
    if (timeoutNanos == 0 && !hasDeadline) { // 没有超时和截止时间
      return; // No timeout and no deadline? Don't bother with the queue.
    }
    inQueue = true; // 标志已经入队
    scheduleTimeout(this, timeoutNanos, hasDeadline);
  }

  // 插入当前超时结点
  private static synchronized void scheduleTimeout(
      AsyncTimeout node, long timeoutNanos, boolean hasDeadline) {
    // Start the watchdog thread and create the head node when the first timeout is scheduled.
    // 第一个结点
    if (head == null) {
      head = new AsyncTimeout();
      new Watchdog().start(); // 启动Watchdog线程
    }

    long now = System.nanoTime();
    // 计算超时时间点
    if (timeoutNanos != 0 && hasDeadline) {
      // Compute the earliest event; either timeout or deadline. Because nanoTime can wrap around,
      // Math.min() is undefined for absolute values, but meaningful for relative ones.
      node.timeoutAt = now + Math.min(timeoutNanos, node.deadlineNanoTime() - now);
    } else if (timeoutNanos != 0) {
      node.timeoutAt = now + timeoutNanos;
    } else if (hasDeadline) {
      node.timeoutAt = node.deadlineNanoTime();
    } else {
      throw new AssertionError();
    }

    // Insert the node in sorted order.
    long remainingNanos = node.remainingNanos(now);
    for (AsyncTimeout prev = head; true; prev = prev.next) {
      // 下一个结点为空或当前结点的剩余时间小于下一个结点的剩余时间，插入当前结点
      if (prev.next == null || remainingNanos < prev.next.remainingNanos(now)) {
        node.next = prev.next;
        prev.next = node;
        if (prev == head) { // 在头结点后面插入结点，唤醒watchdog线程
          AsyncTimeout.class.notify(); // Wake up the watchdog when inserting at the front.
        }
        break;
      }
    }
  }

  /** Returns true if the timeout occurred. 超时返回true */
  public final boolean exit() {
    if (!inQueue) return false;
    inQueue = false;
    return cancelScheduledTimeout(this); // 移除当前结点
  }

  /** Returns true if the timeout occurred. 移除当前超时结点 */
  private static synchronized boolean cancelScheduledTimeout(AsyncTimeout node) {
    // Remove the node from the linked list.
    for (AsyncTimeout prev = head; prev != null; prev = prev.next) {
      if (prev.next == node) {
        prev.next = node.next;
        node.next = null;
        return false;
      }
    }

    // The node wasn't found in the linked list: it must have timed out!
    // 结点没在队列中，已经超时
    return true;
  }

  /**
   * Returns the amount of time left until the time out. This will be negative if the timeout has
   * elapsed and the timeout should occur immediately.
   * 距离超时剩下的时间纳秒数
   */
  private long remainingNanos(long now) {
    return timeoutAt - now;
  }

  /**
   * Invoked by the watchdog thread when the time between calls to {@link #enter()} and {@link
   * #exit()} has exceeded the timeout.
   */
  protected void timedOut() {
  }

  /**
   * Returns a new sink that delegates to {@code sink}, using this to implement timeouts. This works
   * best if {@link #timedOut} is overridden to interrupt {@code sink}'s current operation.
   * 封装sink增加超时处理
   */
  public final Sink sink(final Sink sink) {
    return new Sink() {

      @Override public void write(Buffer source, long byteCount) throws IOException {
        checkOffsetAndCount(source.size, 0, byteCount); // 检查是否越界

        while (byteCount > 0L) {
          // Count how many bytes to write. This loop guarantees we split on a segment boundary.
          long toWrite = 0L;
          for (Segment s = source.head; toWrite < TIMEOUT_WRITE_SIZE; s = s.next) {
            int segmentSize = s.limit - s.pos;
            toWrite += segmentSize;
            if (toWrite >= byteCount) {
              toWrite = byteCount;
              break;
            }
          }

          // Emit one write. Only this section is subject to the timeout.
          boolean throwOnTimeout = false;
          enter(); // 计算超时加入队列
          try {
            sink.write(source, toWrite); // 读取source中数据写到sink
            byteCount -= toWrite;
            throwOnTimeout = true;
          } catch (IOException e) {
            throw exit(e);
          } finally {
            exit(throwOnTimeout); // 移除当前结点
          }
        }
      }

      @Override public void flush() throws IOException {
        boolean throwOnTimeout = false;
        enter();
        try {
          sink.flush();
          throwOnTimeout = true;
        } catch (IOException e) {
          throw exit(e);
        } finally {
          exit(throwOnTimeout);
        }
      }

      @Override public void close() throws IOException {
        boolean throwOnTimeout = false;
        enter();
        try {
          sink.close();
          throwOnTimeout = true;
        } catch (IOException e) {
          throw exit(e);
        } finally {
          exit(throwOnTimeout);
        }
      }

      @Override public Timeout timeout() {
        return AsyncTimeout.this;
      }

      @Override public String toString() {
        return "AsyncTimeout.sink(" + sink + ")";
      }
    };
  }

  /**
   * Returns a new source that delegates to {@code source}, using this to implement timeouts. This
   * works best if {@link #timedOut} is overridden to interrupt {@code sink}'s current operation.
   * 封装source增加超时处理
   */
  public final Source source(final Source source) {
    return new Source() {

      @Override public long read(Buffer sink, long byteCount) throws IOException {
        boolean throwOnTimeout = false;
        enter();
        try {
          long result = source.read(sink, byteCount);
          throwOnTimeout = true;
          return result;
        } catch (IOException e) {
          throw exit(e);
        } finally {
          exit(throwOnTimeout);
        }
      }

      @Override public void close() throws IOException {
        boolean throwOnTimeout = false;
        enter();
        try {
          source.close();
          throwOnTimeout = true;
        } catch (IOException e) {
          throw exit(e);
        } finally {
          exit(throwOnTimeout);
        }
      }

      @Override public Timeout timeout() {
        return AsyncTimeout.this;
      }

      @Override public String toString() {
        return "AsyncTimeout.source(" + source + ")";
      }
    };
  }

  /**
   * Throws an IOException if {@code throwOnTimeout} is {@code true} and a timeout occurred. See
   * {@link #newTimeoutException(java.io.IOException)} for the type of exception thrown.
   */
  final void exit(boolean throwOnTimeout) throws IOException {
    boolean timedOut = exit();
    if (timedOut && throwOnTimeout) throw newTimeoutException(null);
  }

  /**
   * Returns either {@code cause} or an IOException that's caused by {@code cause} if a timeout
   * occurred. See {@link #newTimeoutException(java.io.IOException)} for the type of exception
   * returned.
   */
  final IOException exit(IOException cause) throws IOException {
    if (!exit()) return cause;
    return newTimeoutException(cause);
  }

  /**
   * Returns an {@link IOException} to represent a timeout. By default this method returns {@link
   * java.io.InterruptedIOException}. If {@code cause} is non-null it is set as the cause of the
   * returned exception.
   */
  protected IOException newTimeoutException(@Nullable IOException cause) {
    InterruptedIOException e = new InterruptedIOException("timeout");
    if (cause != null) {
      e.initCause(cause);
    }
    return e;
  }

  // watchdog线程
  private static final class Watchdog extends Thread {
    Watchdog() {
      super("Okio Watchdog");
      setDaemon(true);
    }

    public void run() {
      while (true) {
        try {
          AsyncTimeout timedOut;
          synchronized (AsyncTimeout.class) {
            timedOut = awaitTimeout();

            // Didn't find a node to interrupt. Try again.
            if (timedOut == null) continue;

            // The queue is completely empty. Let this thread exit and let another watchdog thread
            // get created on the next call to scheduleTimeout().
            if (timedOut == head) { // 队列为空，关闭watchdog线程
              head = null;
              return;
            }
          }

          // Close the timed out node.
          timedOut.timedOut();
        } catch (InterruptedException ignored) { // 忽略异常
        }
      }
    }
  }

  /**
   * Removes and returns the node at the head of the list, waiting for it to time out if necessary.
   * This returns {@link #head} if there was no node at the head of the list when starting, and
   * there continues to be no node after waiting {@code IDLE_TIMEOUT_NANOS}. It returns null if a
   * new node was inserted while waiting. Otherwise this returns the node being waited on that has
   * been removed.
   */
  static @Nullable AsyncTimeout awaitTimeout() throws InterruptedException {
    // Get the next eligible node.
    AsyncTimeout node = head.next;

    // The queue is empty. Wait until either something is enqueued or the idle timeout elapses.
    if (node == null) {
      long startNanos = System.nanoTime();
      AsyncTimeout.class.wait(IDLE_TIMEOUT_MILLIS); // 队列为空，等待被唤醒（插入第一个结点时会唤醒）
      return head.next == null && (System.nanoTime() - startNanos) >= IDLE_TIMEOUT_NANOS
          ? head  // The idle timeout elapsed.
          : null; // The situation has changed.
    }

    long waitNanos = node.remainingNanos(System.nanoTime());

    // The head of the queue hasn't timed out yet. Await that.
    // 第一个结点还没有超时（后面的也不会超时）
    if (waitNanos > 0) {
      // Waiting is made complicated by the fact that we work in nanoseconds,
      // but the API wants (millis, nanos) in two arguments.
      long waitMillis = waitNanos / 1000000L;
      waitNanos -= (waitMillis * 1000000L);
      AsyncTimeout.class.wait(waitMillis, (int) waitNanos);
      return null;
    }

    // The head of the queue has timed out. Remove it.
    // 第一个结点超时移除
    head.next = node.next;
    node.next = null;
    return node;
  }
}
