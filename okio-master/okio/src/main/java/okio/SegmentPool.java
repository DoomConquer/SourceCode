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

import javax.annotation.Nullable;

/**
 * A collection of unused segments, necessary to avoid GC churn and zero-fill.
 * This pool is a thread-safe static singleton.
 * segment池，线程安全
 */
final class SegmentPool {
  /** The maximum number of bytes to pool. segment池最大容量 */
  // TODO: Is 64 KiB a good maximum size? Do we ever have that many idle segments?
  static final long MAX_SIZE = 64 * 1024; // 64 KiB.

  /** Singly-linked list of segments. segment链表头结点 */
  static @Nullable Segment next;

  /** Total bytes in this pool. 池中当前空闲字节数 */
  static long byteCount;

  private SegmentPool() {
  }

  // 取出池中segment使用
  static Segment take() {
    synchronized (SegmentPool.class) {
      if (next != null) {
        Segment result = next;
        next = result.next;
        result.next = null;
        byteCount -= Segment.SIZE;
        return result;
      }
    }
    return new Segment(); // Pool is empty. Don't zero-fill while holding a lock.
  }

  // 回收segment放回池中
  static void recycle(Segment segment) {
    if (segment.next != null || segment.prev != null) throw new IllegalArgumentException();
    if (segment.shared) return; // This segment cannot be recycled.
    // 将segment加到池中链表头部
    synchronized (SegmentPool.class) {
      if (byteCount + Segment.SIZE > MAX_SIZE) return; // Pool is full.
      byteCount += Segment.SIZE; // 池中总字节数增加
      segment.next = next;
      segment.pos = segment.limit = 0; // 重置pos和limit
      next = segment;
    }
  }
}
