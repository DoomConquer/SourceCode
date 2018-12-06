/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* This file copied from Hadoop's security branch,
  * with the following changes:
  * 1. package changed from org.apache.hadoop.util to
  *    org.apache.zookeeper.
  * 2. Usage of Hadoop's Configuration class removed since
  *    it is not available in Zookeeper: instead, system properties
  *    are used.
  * 3. The deprecated getUlimitMemoryCommand() method removed since
  *    it is not needed.
  */


package org.apache.zookeeper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.log4j.Logger;
import org.apache.zookeeper.common.Time;

/**
 * A base class for running a Unix command.
 * 该类主要用于执行unix命令
 * 
 * <code>Shell</code> can be used to run unix commands like <code>du</code> or
 * <code>df</code>. It also offers facilities to gate commands by 
 * time-intervals.
 */
abstract public class Shell {
  
  Logger LOG = Logger.getLogger(Shell.class);
  
  /** a Unix command to get the current user's name */
  public final static String USER_NAME_COMMAND = "whoami"; // 查看当前用户
  /** a Unix command to get the current user's groups list */
  public static String[] getGroupsCommand() {
    return new String[]{"bash", "-c", "groups"};
  } // 查看当前用户所在用户组

  /** a Unix command to get a given user's groups list */
  public static String[] getGroupsForUserCommand(final String user) { // 获取user所在用户组
    //'groups username' command return is non-consistent across different unixes
    return new String [] {"bash", "-c", "id -Gn " + user};
  }

  /** a Unix command to set permission */
  public static final String SET_PERMISSION_COMMAND = "chmod"; // 设置权限
  /** a Unix command to set owner */
  public static final String SET_OWNER_COMMAND = "chown"; // change owner
  public static final String SET_GROUP_COMMAND = "chgrp"; // change group

  /** Return a Unix command to get permission information. */
  public static String[] getGET_PERMISSION_COMMAND() {
    //force /bin/ls, except on windows.
    return new String[] {(WINDOWS ? "ls" : "/bin/ls"), "-ld"};
  }

  /**Time after which the executing script would be timedout*/
  protected long timeOutInterval = 0L;
  /** If or not script timed out*/
  private AtomicBoolean timedOut;

  /** a Unix command to get ulimit of a process. */
  public static final String ULIMIT_COMMAND = "ulimit"; // 进程所占用的资源
  
  /** 
   * Get the Unix command for setting the maximum virtual memory available
   * to a given child process. This is only relevant when we are forking a
   * process from within the Mapper or the Reducer implementations.
   * Also see Hadoop Pipes and Hadoop Streaming.
   * 设置最大虚拟内存
   * 
   * It also checks to ensure that we are running on a *nix platform else 
   * (e.g. in Cygwin/Windows) it returns <code>null</code>.
   * @param memoryLimit virtual memory limit
   * @return a <code>String[]</code> with the ulimit command arguments or 
   *         <code>null</code> if we are running on a non *nix platform or
   *         if the limit is unspecified.
   */
  public static String[] getUlimitMemoryCommand(int memoryLimit) {
    // ulimit isn't supported on Windows（Windows环境下不支持）
    if (WINDOWS) {
      return null;
    }
    return new String[] {ULIMIT_COMMAND, "-v", String.valueOf(memoryLimit)};
  }

  /** Set to true on Windows platforms 是否Windows平台（windows平台可以使用Cygwin运行Unix命令）*/
  public static final boolean WINDOWS /* borrowed from Path.WINDOWS */
                = System.getProperty("os.name").startsWith("Windows");
  
  private long    interval;   // refresh interval in msec 执行下一次命令时间间隔
  private long    lastTime;   // last time the command was performed
  private Map<String, String> environment; // env for the command execution
  private File dir;
  private Process process; // sub process used to execute the command
  private int exitCode;

  /**If or not script finished executing*/
  private volatile AtomicBoolean completed; // 脚本是否执行完成
  
  public Shell() {
    this(0L);
  }
  
  /**
   * @param interval the minimum duration to wait before re-executing the 
   *        command.
   */
  public Shell( long interval ) {
    this.interval = interval;
    this.lastTime = (interval < 0) ? 0 : -interval;
  }
  
  /** set the environment for the command 
   * @param env Mapping of environment variables
   */
  protected void setEnvironment(Map<String, String> env) {
    this.environment = env;
  }

  /** set the working directory 设置工作目录
   * @param dir The directory where the command would be executed
   */
  protected void setWorkingDirectory(File dir) {
    this.dir = dir;
  }

  /** check to see if a command needs to be executed and execute if needed */
  protected void run() throws IOException {
    if (lastTime + interval > Time.currentElapsedTime()) // 时间间隔不够
      return;
    exitCode = 0; // reset for next run
    runCommand(); // 执行命令
  }

  /** Run a command 执行命令 */
  private void runCommand() throws IOException { 
    ProcessBuilder builder = new ProcessBuilder(getExecString());
    Timer timeOutTimer = null;
    ShellTimeoutTimerTask timeoutTimerTask = null;
    timedOut = new AtomicBoolean(false);
    completed = new AtomicBoolean(false);
    
    if (environment != null) {
      builder.environment().putAll(this.environment);
    }
    if (dir != null) {
      builder.directory(this.dir);
    }
    
    process = builder.start();
    if (timeOutInterval > 0) { // 0不会超时
      timeOutTimer = new Timer();
      timeoutTimerTask = new ShellTimeoutTimerTask(this);
      //One time scheduling.
      timeOutTimer.schedule(timeoutTimerTask, timeOutInterval); // 超时定时器
    }
    final BufferedReader errReader = 
            new BufferedReader(new InputStreamReader(process
                                                     .getErrorStream()));
    BufferedReader inReader = 
            new BufferedReader(new InputStreamReader(process
                                                     .getInputStream()));
    final StringBuffer errMsg = new StringBuffer(); // 错误信息
    
    // read error and input streams as this would free up the buffers
    // free the error stream buffer
    Thread errThread = new Thread() {
      @Override
      public void run() { // 读取错误信息
        try {
          String line = errReader.readLine();
          while((line != null) && !isInterrupted()) {
            errMsg.append(line);
            errMsg.append(System.getProperty("line.separator"));
            line = errReader.readLine();
          }
        } catch(IOException ioe) {
          LOG.warn("Error reading the error stream", ioe);
        }
      }
    };
    try {
      errThread.start();
    } catch (IllegalStateException ise) { }
    try {
      parseExecResult(inReader); // parse the output
      // clear the input stream buffer
      String line = inReader.readLine();
      while(line != null) { 
        line = inReader.readLine();
      }
      // wait for the process to finish and check the exit code
      // 等待命令执行完成
      exitCode  = process.waitFor();
      try {
        // make sure that the error thread exits
        errThread.join();
      } catch (InterruptedException ie) {
        LOG.warn("Interrupted while reading the error stream", ie);
      }
      completed.set(true); // 完成执行
      //the timeout thread handling
      //taken care in finally block
      if (exitCode != 0) {
        throw new ExitCodeException(exitCode, errMsg.toString());
      }
    } catch (InterruptedException ie) {
        throw new IOException(ie.toString());
    } finally {
        if ((timeOutTimer != null) && !timedOut.get()) {
          timeOutTimer.cancel(); // 取消超时定时器
        }
        // close the input stream 关闭流
        try {
          inReader.close();
        } catch (IOException ioe) {
          LOG.warn("Error while closing the input stream", ioe);
        }
        if (!completed.get()) { // 如果没执行完成（主线程被中断或异常），中断收集错误线程
          errThread.interrupt();
        }
        try {
          errReader.close();
        } catch (IOException ioe) {
          LOG.warn("Error while closing the error stream", ioe);
        }
        process.destroy();
        lastTime = Time.currentElapsedTime();
    }
  }

  /** return an array containing the command name & its parameters 获取执行的命令*/
  protected abstract String[] getExecString();
  
  /** Parse the execution result */
  protected abstract void parseExecResult(BufferedReader lines) throws IOException;

  /** get the current sub-process executing the given command 
   * @return process executing the command
   */
  public Process getProcess() {
    return process;
  }

  /** get the exit code 
   * @return the exit code of the process
   */
  public int getExitCode() {
    return exitCode;
  }

  /**
   * This is an IOException with exit code added.
   * 带exitCode的IO异常
   */
  @SuppressWarnings("serial")
  public static class ExitCodeException extends IOException {
    int exitCode;
    
    public ExitCodeException(int exitCode, String message) {
      super(message);
      this.exitCode = exitCode;
    }
    
    public int getExitCode() {
      return exitCode;
    }
  }
  
  /**
   * A simple shell command executor. 一个简单的命令执行器
   * 
   * <code>ShellCommandExecutor</code>should be used in cases where the output 
   * of the command needs no explicit parsing and where the command, working 
   * directory and the environment remains unchanged. The output of the command 
   * is stored as-is and is expected to be small.
   */
  public static class ShellCommandExecutor extends Shell {
    
    private String[] command;    // 命令
    private StringBuffer output; // 结果
    
    
    public ShellCommandExecutor(String[] execString) {
      this(execString, null);
    }
    
    public ShellCommandExecutor(String[] execString, File dir) {
      this(execString, dir, null);
    }
   
    public ShellCommandExecutor(String[] execString, File dir, Map<String, String> env) {
      this(execString, dir, env , 0L);
    }

    /**
     * Create a new instance of the ShellCommandExecutor to execute a command.
     * 
     * @param execString The command to execute with arguments
     * @param dir If not-null, specifies the directory which should be set
     *            as the current working directory for the command.
     *            If null, the current working directory is not modified.
     * @param env If not-null, environment of the command will include the
     *            key-value pairs specified in the map. If null, the current
     *            environment is not modified.
     * @param timeout Specifies the time in milliseconds, after which the
     *                command will be killed and the status marked as timedout.
     *                If 0, the command will not be timed out. 超时时间，0不会超时
     */
    public ShellCommandExecutor(String[] execString, File dir, Map<String, String> env, long timeout) {
      command = execString.clone();
      if (dir != null) {
        setWorkingDirectory(dir);
      }
      if (env != null) {
        setEnvironment(env);
      }
      timeOutInterval = timeout;
    }
        

    /** Execute the shell command. */
    public void execute() throws IOException {
      this.run();    
    }

    protected String[] getExecString() {
      return command;
    }

    // 处理执行结果
    protected void parseExecResult(BufferedReader lines) throws IOException {
      output = new StringBuffer();
      char[] buf = new char[512];
      int nRead;
      while ( (nRead = lines.read(buf, 0, buf.length)) > 0 ) {
        output.append(buf, 0, nRead);
      }
    }
    
    /** Get the output of the shell command.*/
    public String getOutput() {
      return (output == null) ? "" : output.toString();
    }

    /**
     * Returns the commands of this instance. 返回命令
     * Arguments with spaces in are presented with quotes round; other
     * arguments are presented raw
     *
     * @return a string representation of the object.
     */
    public String toString() {
      StringBuilder builder = new StringBuilder();
      String[] args = getExecString();
      for (String s : args) {
        if (s.indexOf(' ') >= 0) {
          builder.append('"').append(s).append('"');
        } else {
          builder.append(s);
        }
        builder.append(' ');
      }
      return builder.toString();
    }
  }
  
  /**
   * To check if the passed script to shell command executor timed out or
   * not. 是否超时
   * 
   * @return if the script timed out.
   */
  public boolean isTimedOut() {
    return timedOut.get();
  }
  
  /**
   * Set if the command has timed out.
   * 
   */
  private void setTimedOut() {
    this.timedOut.set(true);
  }
  
  /** 
   * Static method to execute a shell command. 
   * Covers most of the simple cases without requiring the user to implement  
   * the <code>Shell</code> interface.
   * @param cmd shell command to execute.
   * @return the output of the executed command.
   */
  public static String execCommand(String ... cmd) throws IOException {
    return execCommand(null, cmd, 0L);
  }
  
  /** 
   * Static method to execute a shell command. 执行命令
   * Covers most of the simple cases without requiring the user to implement  
   * the <code>Shell</code> interface.
   * @param env the map of environment key=value
   * @param cmd shell command to execute.
   * @param timeout time in milliseconds after which script should be marked timeout
   * @return the output of the executed command.o
   */
  public static String execCommand(Map<String, String> env, String[] cmd,
      long timeout) throws IOException {
    ShellCommandExecutor exec = new ShellCommandExecutor(cmd, null, env, timeout);
    exec.execute();
    return exec.getOutput();
  }

  /** 
   * Static method to execute a shell command. 
   * Covers most of the simple cases without requiring the user to implement  
   * the <code>Shell</code> interface.
   * @param env the map of environment key=value
   * @param cmd shell command to execute.
   * @return the output of the executed command.
   */
  public static String execCommand(Map<String,String> env, String ... cmd) 
  throws IOException {
    return execCommand(env, cmd, 0L);
  }
  
  /**
   * Timer which is used to timeout scripts spawned off by shell.
   * shell执行命令超时task
   */
  private static class ShellTimeoutTimerTask extends TimerTask {

    private Shell shell;

    public ShellTimeoutTimerTask(Shell shell) {
      this.shell = shell;
    }

    @Override
    public void run() {
      Process p = shell.getProcess();
      try {
        p.exitValue();
      } catch (Exception e) {
        //Process has not terminated.
        //So check if it has completed 
        //if not just destroy it.
        if (p != null && !shell.completed.get()) {
          shell.setTimedOut(); // 设置shell超时
          p.destroy(); // 销毁process
        }
      }
    }
  }
}
