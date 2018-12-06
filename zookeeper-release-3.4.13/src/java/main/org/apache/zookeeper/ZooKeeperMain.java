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

package org.apache.zookeeper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.yetus.audience.InterfaceAudience;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.zookeeper.AsyncCallback.DataCallback;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The command line client to ZooKeeper.
 * zookeeper客户端命令行工具类
 *
 */
@InterfaceAudience.Public
public class ZooKeeperMain {
    private static final Logger LOG = LoggerFactory.getLogger(ZooKeeperMain.class);
    static final Map<String,String> commandMap = new HashMap<String,String>(); // 命令map

    protected MyCommandOptions cl = new MyCommandOptions();
    protected HashMap<Integer,String> history = new HashMap<Integer,String>();
    protected int commandCount = 0; // 执行命令数
    protected boolean printWatches = true;

    protected ZooKeeper zk;
    protected String host = "";

    public boolean getPrintWatches() {
        return printWatches;
    }

    // 初始化命令、参数
    static {
        commandMap.put("connect", "host:port");
        commandMap.put("close","");
        commandMap.put("create", "[-s] [-e] path data acl");
        commandMap.put("delete","path [version]");
        commandMap.put("rmr","path");
        commandMap.put("set","path data [version]");
        commandMap.put("get","path [watch]");
        commandMap.put("ls","path [watch]");
        commandMap.put("ls2","path [watch]");
        commandMap.put("getAcl","path");
        commandMap.put("setAcl","path acl");
        commandMap.put("stat","path [watch]");
        commandMap.put("sync","path");
        commandMap.put("setquota","-n|-b val path");
        commandMap.put("listquota","path");
        commandMap.put("delquota","[-n|-b] path");
        commandMap.put("history","");
        commandMap.put("redo","cmdno");
        commandMap.put("printwatches", "on|off");
        commandMap.put("quit","");
        commandMap.put("addauth", "scheme auth");
    }

    // 输出使用方法
    static void usage() {
        System.err.println("ZooKeeper -server host:port cmd args");
        for (Map.Entry<String, String> entry : commandMap.entrySet()) {
            System.err.println("\t" + entry.getKey() + " " + entry.getValue());
        }
    }

    // 输出watch信息，不做真正的监视器
    private class MyWatcher implements Watcher {
        public void process(WatchedEvent event) {
            if (getPrintWatches()) {
                ZooKeeperMain.printMessage("WATCHER::");
                ZooKeeperMain.printMessage(event.toString());
            }
        }
    }

    // 从字符串permString中获取权限
    static private int getPermFromString(String permString) {
        int perm = 0;
        for (int i = 0; i < permString.length(); i++) {
            switch (permString.charAt(i)) {
            case 'r':
                perm |= ZooDefs.Perms.READ;
                break;
            case 'w':
                perm |= ZooDefs.Perms.WRITE;
                break;
            case 'c':
                perm |= ZooDefs.Perms.CREATE;
                break;
            case 'd':
                perm |= ZooDefs.Perms.DELETE;
                break;
            case 'a':
                perm |= ZooDefs.Perms.ADMIN;
                break;
            default:
                System.err.println("Unknown perm type: " + permString.charAt(i));
            }
        }
        return perm;
    }

    // 输出节点信息
    private static void printStat(Stat stat) {
        System.err.println("cZxid = 0x" + Long.toHexString(stat.getCzxid()));
        System.err.println("ctime = " + new Date(stat.getCtime()).toString());
        System.err.println("mZxid = 0x" + Long.toHexString(stat.getMzxid()));
        System.err.println("mtime = " + new Date(stat.getMtime()).toString());
        System.err.println("pZxid = 0x" + Long.toHexString(stat.getPzxid()));
        System.err.println("cversion = " + stat.getCversion());
        System.err.println("dataVersion = " + stat.getVersion());
        System.err.println("aclVersion = " + stat.getAversion());
        System.err.println("ephemeralOwner = 0x"
        		+ Long.toHexString(stat.getEphemeralOwner()));
        System.err.println("dataLength = " + stat.getDataLength());
        System.err.println("numChildren = " + stat.getNumChildren());
    }

    /**
     * A storage class for both command line options and shell commands.
     * 存放命令选项、参数
     */
    static class MyCommandOptions {

        private Map<String, String> options = new HashMap<String,String>(); // 选项
        private List<String> cmdArgs = null; // 参数
        private String command = null;       // 命令
        // 参数正则匹配
        public static final Pattern ARGS_PATTERN = Pattern.compile("\\s*([^\"\']\\S*|\"[^\"]*\"|'[^']*')\\s*");
        public static final Pattern QUOTED_PATTERN = Pattern.compile("^([\'\"])(.*)(\\1)$");

        public MyCommandOptions() {
          options.put("server", "localhost:2181");
          options.put("timeout", "30000");
        }

        public String getOption(String opt) {
            return options.get(opt);
        }

        public String getCommand( ) {
            return command;
        }

        public String getCmdArgument( int index ) {
            return cmdArgs.get(index);
        }

        // 参数个数
        public int getNumArguments( ) {
            return cmdArgs.size();
        }

        // 参数
        public String[] getArgArray() {
            return cmdArgs.toArray(new String[0]);
        }

        /**
         * Parses a command line that may contain one or more flags
         * before an optional command string 解析命令行
         * @param args command line arguments
         * @return true if parsing succeeded, false otherwise.
         */
        public boolean parseOptions(String[] args) {
            List<String> argList = Arrays.asList(args);
            Iterator<String> it = argList.iterator();

            while (it.hasNext()) {
                String opt = it.next();
                try {
                    if (opt.equals("-server")) {
                        options.put("server", it.next());
                    } else if (opt.equals("-timeout")) {
                        options.put("timeout", it.next());
                    } else if (opt.equals("-r")) {
                        options.put("readonly", "true");
                    }
                } catch (NoSuchElementException e){
                    System.err.println("Error: no argument found for option "
                            + opt);
                    return false;
                }

                // 参数
                if (!opt.startsWith("-")) {
                    command = opt;
                    cmdArgs = new ArrayList<String>( );
                    cmdArgs.add( command );
                    while (it.hasNext()) {
                        cmdArgs.add(it.next());
                    }
                    return true;
                }
            }
            return true;
        }

        /**
         * Breaks a string into command + arguments. 解析命令参数
         * @param cmdstring string of form "cmd arg1 arg2..etc"
         * @return true if parsing succeeded.
         */
        public boolean parseCommand( String cmdstring ) {
            Matcher matcher = ARGS_PATTERN.matcher(cmdstring);

            List<String> args = new LinkedList<String>();
            while (matcher.find()) {
                String value = matcher.group(1);
                if (QUOTED_PATTERN.matcher(value).matches()) {
                    // Strip off the surrounding quotes
                    value = value.substring(1, value.length() - 1);
                }
                args.add(value);
            }
            if (args.isEmpty()){
                return false;
            }
            command = args.get(0);
            cmdArgs = args;
            return true;
        }
    }


    /**
     * Makes a list of possible completions, either for commands
     * or for zk nodes if the token to complete begins with /
     * 保存历史命令
     */
    protected void addToHistory(int i, String cmd) {
        history.put(i, cmd);
    }

    public static List<String> getCommands() {
        return new LinkedList<String>(commandMap.keySet());
    }

    // zk简单信息
    protected String getPrompt() {       
        return "[zk: " + host + "(" + zk.getState() + ")" + " " + commandCount + "] ";
    }

    // 输出消息
    public static void printMessage(String msg) {
        System.out.println("\n" + msg);
    }

    // 连接服务端
    protected void connectToZK(String newHost) throws InterruptedException, IOException {
        if (zk != null && zk.getState().isAlive()) { // 如果已存在，先关闭
            zk.close();
        }
        host = newHost;
        boolean readOnly = cl.getOption("readonly") != null;
        zk = new ZooKeeper(host,
                 Integer.parseInt(cl.getOption("timeout")),
                 new MyWatcher(), readOnly);
    }

    // main方法
    public static void main(String args[])
        throws KeeperException, IOException, InterruptedException {
        ZooKeeperMain main = new ZooKeeperMain(args);
        main.run();
    }

    public ZooKeeperMain(String args[]) throws IOException, InterruptedException {
        cl.parseOptions(args);
        System.out.println("Connecting to " + cl.getOption("server"));
        connectToZK(cl.getOption("server"));
        //zk = new ZooKeeper(cl.getOption("server"), Integer.parseInt(cl.getOption("timeout")), new MyWatcher());
    }

    public ZooKeeperMain(ZooKeeper zk) {
      this.zk = zk;
    }

    // 根据输入或jline执行命令
    @SuppressWarnings("unchecked")
    void run() throws KeeperException, IOException, InterruptedException {
        if (cl.getCommand() == null) {
            System.out.println("Welcome to ZooKeeper!");

            boolean jlinemissing = false;
            // only use jline if it's in the classpath 反射使用jline
            try {
                Class<?> consoleC = Class.forName("jline.ConsoleReader");
                Class<?> completorC = Class.forName("org.apache.zookeeper.JLineZNodeCompletor");

                System.out.println("JLine support is enabled");

                Object console = consoleC.getConstructor().newInstance();

                Object completor = completorC.getConstructor(ZooKeeper.class).newInstance(zk); // 命令补全
                Method addCompletor = consoleC.getMethod("addCompletor", Class.forName("jline.Completor"));
                addCompletor.invoke(console, completor);

                String line;
                Method readLine = consoleC.getMethod("readLine", String.class);
                while ((line = (String)readLine.invoke(console, getPrompt())) != null) {
                    executeLine(line); // 执行
                }
            } catch (ClassNotFoundException e) { // 不支持jline
                LOG.debug("Unable to start jline", e);
                jlinemissing = true;
            } catch (NoSuchMethodException e) {
                LOG.debug("Unable to start jline", e);
                jlinemissing = true;
            } catch (InvocationTargetException e) {
                LOG.debug("Unable to start jline", e);
                jlinemissing = true;
            } catch (IllegalAccessException e) {
                LOG.debug("Unable to start jline", e);
                jlinemissing = true;
            } catch (InstantiationException e) {
                LOG.debug("Unable to start jline", e);
                jlinemissing = true;
            }

            if (jlinemissing) { // 不支持jline，直接使用标准输入
                System.out.println("JLine support is disabled");
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

                String line;
                while ((line = br.readLine()) != null) {
                    executeLine(line);
                }
            }
        } else {
            // Command line args non-null.  Run what was passed.
            processCmd(cl);
        }
    }

    // 执行输入行命令
    public void executeLine(String line)
    throws InterruptedException, IOException, KeeperException {
      if (!line.equals("")) {
        cl.parseCommand(line);
        addToHistory(commandCount, line); // 添加到执行命令历史中
        processCmd(cl);
        commandCount++;
      }
    }

    // 获取数据异步回调
    private static DataCallback dataCallback = new DataCallback() {

        public void processResult(int rc, String path, Object ctx, byte[] data,
                Stat stat) {
            System.out.println("rc = " + rc + " path = " + path + " data = "
                    + (data == null ? "null" : new String(data)) + " stat = ");
            printStat(stat);
        }

    };

    /**
     * trim the quota tree to recover unwanted tree elements
     * in the quota's tree 修剪path路径以上的结点的配额结点（去掉没有子结点的配额结点）
     * @param zk the zookeeper client
     * @param path the path to start from and go up and see if their
     * is any unwanted parent in the path.
     * @return true if sucessful
     * @throws KeeperException
     * @throws IOException
     * @throws InterruptedException
     */
    private static boolean trimProcQuotas(ZooKeeper zk, String path)
        throws KeeperException, IOException, InterruptedException {
        if (Quotas.quotaZookeeper.equals(path)) {
            return true;
        }
        List<String> children = zk.getChildren(path, false);
        if (children.size() == 0) {
            zk.delete(path, -1);
            String parent = path.substring(0, path.lastIndexOf('/'));
            return trimProcQuotas(zk, parent); // 递归修剪父结点
        } else {
            return true;
        }
    }

    /**
     * this method deletes quota for a node. 删除path路径配额结点
     * @param zk the zookeeper client
     * @param path the path to delete quota for
     * @param bytes true if number of bytes needs to
     * be unset 是否需要重置bytes
     * @param numNodes true if number of nodes needs
     * to be unset 是否需要重置numNodes
     * @return true if quota deletion is successful
     * @throws KeeperException
     * @throws IOException
     * @throws InterruptedException
     */
    public static boolean delQuota(ZooKeeper zk, String path,
            boolean bytes, boolean numNodes)
        throws KeeperException, IOException, InterruptedException {
        String parentPath = Quotas.quotaZookeeper + path; // 父结点路径
        String quotaPath = Quotas.quotaZookeeper + path + "/" + Quotas.limitNode; // 配额限制结点路径
        if (zk.exists(quotaPath, false) == null) { // 不存在配额结点
            System.out.println("Quota does not exist for " + path);
            return true;
        }
        byte[] data = null;
        try {
            data = zk.getData(quotaPath, false, new Stat()); // 获取配额结点数据
        } catch(KeeperException.NoNodeException ne) {
            System.err.println("quota does not exist for " + path);
            return true;
        }
        StatsTrack strack = new StatsTrack(new String(data));
        if (bytes && !numNodes) {
            strack.setBytes(-1L);
            zk.setData(quotaPath, strack.toString().getBytes(), -1);
        } else if (!bytes && numNodes) {
            strack.setCount(-1);
            zk.setData(quotaPath, strack.toString().getBytes(), -1);
        } else if (bytes && numNodes) { // 删除结点
            // delete till you can find a node with more than
            // one child
            List<String> children = zk.getChildren(parentPath, false);
            /// delete the direct children first
            for (String child : children) { // 删除子结点
                zk.delete(parentPath + "/" + child, -1);
            }
            // cut the tree till their is more than one child
            trimProcQuotas(zk, parentPath); // 删除该路径往上没有子结点的结点
        }
        return true;
    }

    // 检查path路径上是否有结点已经有配额限制，如果有抛出异常
    private static void checkIfParentQuota(ZooKeeper zk, String path)
        throws InterruptedException, KeeperException {
        final String[] splits = path.split("/");
        String quotaPath = Quotas.quotaZookeeper;
        for (String str : splits) {
            if (str.length() == 0) {
                // this should only be for the beginning of the path
                // i.e. "/..." - split(path)[0] is empty string before first '/'
                continue;
            }
            quotaPath += "/" + str;
            List<String> children =  null;
            try {
                children = zk.getChildren(quotaPath, false);
            } catch(KeeperException.NoNodeException ne) {
                LOG.debug("child removed during quota check", ne);
                return;
            }
            if (children.size() == 0) {
                return;
            }
            for (String child : children) {
                if (Quotas.limitNode.equals(child)) {
                    throw new IllegalArgumentException(path + " has a parent "
                            + quotaPath + " which has a quota");
                }
            }
        }
    }

    /**
     * this method creates a quota node for the path 创建配额结点信息（实际结点（DataTree上）必须已经存在）
     * @param zk the ZooKeeper client
     * @param path the path for which quota needs to be created
     * @param bytes the limit of bytes on this path
     * @param numNodes the limit of number of nodes on this path
     * @return true if its successful and false if not.
     */
    public static boolean createQuota(ZooKeeper zk, String path,
            long bytes, int numNodes)
        throws KeeperException, IOException, InterruptedException {
        // check if the path exists. We cannot create
        // quota for a path that already exists in zookeeper
        // for now.
        Stat initStat = zk.exists(path, false);
        if (initStat == null) { // 结点不存在
            throw new IllegalArgumentException(path + " does not exist.");
        }
        // now check if their is already existing
        // parent or child that has quota

        String quotaPath = Quotas.quotaZookeeper;
        // check for more than 2 children --
        // if zookeeper_stats and zookeeper_qutoas
        // are not the children then this path
        // is an ancestor of some path that
        // already has quota
        String realPath = Quotas.quotaZookeeper + path; // 配额结点路径
        try {
            List<String> children = zk.getChildren(realPath, false);
            for (String child : children) {
                if (!child.startsWith("zookeeper_")) {
                    throw new IllegalArgumentException(path + " has child " +
                            child + " which has a quota");
                }
            }
        } catch(KeeperException.NoNodeException ne) {
            // this is fine
        }

        //check for any parent that has been quota
        // 检查父结点是否已经有配额限制
        checkIfParentQuota(zk, path);

        // this is valid node for quota
        // start creating all the parents
        if (zk.exists(quotaPath, false) == null) { // 根节点不存在，创建根结点
            try {
                zk.create(Quotas.procZookeeper, null, Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
                zk.create(Quotas.quotaZookeeper, null, Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
            } catch(KeeperException.NodeExistsException ne) {
                // do nothing
            }
        }

        // now create the direct children
        // and the stat and quota nodes
        String[] splits = path.split("/");
        StringBuilder sb = new StringBuilder();
        sb.append(quotaPath);
        for (int i = 1; i < splits.length; i++) {
            sb.append("/" + splits[i]);
            quotaPath = sb.toString();
            try { // 创建path对应的配额结点
                zk.create(quotaPath, null, Ids.OPEN_ACL_UNSAFE ,
                        CreateMode.PERSISTENT);
            } catch(KeeperException.NodeExistsException ne) {
                //do nothing
            }
        }
        String statPath = quotaPath + "/" + Quotas.statNode; // 配额状态结点路径
        quotaPath = quotaPath + "/" + Quotas.limitNode;      // 配额限制结点路径
        StatsTrack strack = new StatsTrack(null);
        strack.setBytes(bytes);
        strack.setCount(numNodes);
        try {
            zk.create(quotaPath, strack.toString().getBytes(),
                    Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT); // 创建配额限制结点
            StatsTrack stats = new StatsTrack(null);
            stats.setBytes(0L);
            stats.setCount(0);
            zk.create(statPath, stats.toString().getBytes(),
                    Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT); // 创建配额状态结点
        } catch(KeeperException.NodeExistsException ne) { // 如果结点存在就更新
            byte[] data = zk.getData(quotaPath, false , new Stat());
            StatsTrack strackC = new StatsTrack(new String(data));
            if (bytes != -1L) {
                strackC.setBytes(bytes);
            }
            if (numNodes != -1) {
                strackC.setCount(numNodes);
            }
            zk.setData(quotaPath, strackC.toString().getBytes(), -1);
        }
        return true;
    }

    // 执行cmd
    protected boolean processCmd(MyCommandOptions co)
        throws KeeperException, IOException, InterruptedException {
        try {
            return processZKCmd(co);
        } catch (IllegalArgumentException e) {
            System.err.println("Command failed: " + e);
        } catch (KeeperException.NoNodeException e) {
            System.err.println("Node does not exist: " + e.getPath());
        } catch (KeeperException.NoChildrenForEphemeralsException e) {
            System.err.println("Ephemerals cannot have children: " + e.getPath());
        } catch (KeeperException.NodeExistsException e) {
            System.err.println("Node already exists: " + e.getPath());
        } catch (KeeperException.NotEmptyException e) {
            System.err.println("Node not empty: " + e.getPath());
        } catch (KeeperException.NotReadOnlyException e) {
            System.err.println("Not a read-only call: " + e.getPath());
        }catch (KeeperException.InvalidACLException  e) {
            System.err.println("Acl is not valid : "+e.getPath());
        }catch (KeeperException.NoAuthException  e) {
            System.err.println("Authentication is not valid : "+e.getPath());
        }catch (KeeperException.BadArgumentsException   e) {
            System.err.println("Arguments are not valid : "+e.getPath());
        }catch (KeeperException.BadVersionException e) {
            System.err.println("version No is not valid : "+e.getPath());
        }
        return false;
    }

    // 执行zk命令
    protected boolean processZKCmd(MyCommandOptions co)
        throws KeeperException, IOException, InterruptedException {
        Stat stat = new Stat();
        String[] args = co.getArgArray();
        String cmd = co.getCommand();
        if (args.length < 1) {
            usage();
            return false;
        }

        if (!commandMap.containsKey(cmd)) { // 不包含可执行的命令
            usage();
            return false;
        }
        
        boolean watch = args.length > 2;
        String path = null;
        List<ACL> acl = Ids.OPEN_ACL_UNSAFE;
        LOG.debug("Processing " + cmd);

        if (cmd.equals("quit")) { // 停止
            System.out.println("Quitting...");
            zk.close();
            System.exit(0);
        } else if (cmd.equals("redo") && args.length >= 2) { // redo
            Integer i = Integer.decode(args[1]);
            if (commandCount <= i || i < 0){ // don't allow redoing this redo
                System.out.println("Command index out of range");
                return false;
            }
            cl.parseCommand(history.get(i));
            if (cl.getCommand().equals( "redo" )){
                System.out.println("No redoing redos");
                return false;
            }
            history.put(commandCount, history.get(i));
            processCmd( cl);
        } else if (cmd.equals("history")) { // 实现历史命令
            for (int i = commandCount - 10; i <= commandCount; ++i) {
                if (i < 0) continue;
                System.out.println(i + " - " + history.get(i));
            }
        } else if (cmd.equals("printwatches")) { // 输出watch信息
            if (args.length == 1) {
                System.out.println("printwatches is " + (printWatches ? "on" : "off"));
            } else {
                printWatches = args[1].equals("on");
            }
        } else if (cmd.equals("connect")) { // 连接
            if (args.length >= 2) {
                connectToZK(args[1]);
            } else {
                connectToZK(host);
            }
        }
        
        // Below commands all need a live connection 下面的命令需要活动的连接（与服务端保持连接）
        if (zk == null || !zk.getState().isAlive()) {
            System.out.println("Not connected");
            return false;
        }
        
        if (cmd.equals("create") && args.length >= 3) { // 创建
            int first = 0;
            CreateMode flags = CreateMode.PERSISTENT;
            if ((args[1].equals("-e") && args[2].equals("-s"))
                    || (args[1]).equals("-s") && (args[2].equals("-e"))) {
                first += 2;
                flags = CreateMode.EPHEMERAL_SEQUENTIAL;
            } else if (args[1].equals("-e")) {
                first++;
                flags = CreateMode.EPHEMERAL;
            } else if (args[1].equals("-s")) {
                first++;
                flags = CreateMode.PERSISTENT_SEQUENTIAL;
            }
            if (args.length == first + 4) {
                acl = parseACLs(args[first + 3]);
            }
            path = args[first + 1];
            String newPath = zk.create(path, args[first + 2].getBytes(), acl, flags); // 创建结点
            System.err.println("Created " + newPath);
        } else if (cmd.equals("delete") && args.length >= 2) { // 删除
            path = args[1];
            zk.delete(path, watch ? Integer.parseInt(args[2]) : -1);
        } else if (cmd.equals("rmr") && args.length >= 2) { // 递归删除
            path = args[1];
            ZKUtil.deleteRecursive(zk, path);
        } else if (cmd.equals("set") && args.length >= 3) { // 设置
            path = args[1];
            stat = zk.setData(path, args[2].getBytes(), args.length > 3 ? Integer.parseInt(args[3]) : -1);
            printStat(stat);
        } else if (cmd.equals("aget") && args.length >= 2) { // 异步获取数据
            path = args[1];
            zk.getData(path, watch, dataCallback, path);
        } else if (cmd.equals("get") && args.length >= 2) { // 获取数据
            path = args[1];
            byte data[] = zk.getData(path, watch, stat);
            data = (data == null)? "null".getBytes() : data;
            System.out.println(new String(data));
            printStat(stat);
        } else if (cmd.equals("ls") && args.length >= 2) { // 获取结点下子结点
            path = args[1];
            List<String> children = zk.getChildren(path, watch);
            System.out.println(children);
        } else if (cmd.equals("ls2") && args.length >= 2) { // 获取结点下子结点及状态stat
            path = args[1];
            List<String> children = zk.getChildren(path, watch, stat);
            System.out.println(children);
            printStat(stat);
        } else if (cmd.equals("getAcl") && args.length >= 2) { // 获取ACL权限
            path = args[1];
            acl = zk.getACL(path, stat);
            for (ACL a : acl) {
                System.out.println(a.getId() + ": " + getPermString(a.getPerms()));
            }
        } else if (cmd.equals("setAcl") && args.length >= 3) { // 设置ACL权限
            path = args[1];
            stat = zk.setACL(path, parseACLs(args[2]), args.length > 4 ? Integer.parseInt(args[3]) : -1);
            printStat(stat);
        } else if (cmd.equals("stat") && args.length >= 2) { // 输出stat
            path = args[1];
            stat = zk.exists(path, watch);
            if (stat == null) {
              throw new KeeperException.NoNodeException(path);
            }
            printStat(stat);
        } else if (cmd.equals("listquota") && args.length >= 2) { // 显示结点配额信息
            path = args[1];
            String absolutePath = Quotas.quotaZookeeper + path + "/" + Quotas.limitNode;
            byte[] data =  null;
            try {
                System.err.println("absolute path is " + absolutePath);
                data = zk.getData(absolutePath, false, stat);
                StatsTrack st = new StatsTrack(new String(data));
                System.out.println("Output quota for " + path + " " + st.toString());

                data = zk.getData(Quotas.quotaZookeeper + path + "/" +
                        Quotas.statNode, false, stat);
                System.out.println("Output stat for " + path + " " +
                        new StatsTrack(new String(data)).toString());
            } catch(KeeperException.NoNodeException ne) {
                System.err.println("quota for " + path + " does not exist.");
            }
        } else if (cmd.equals("setquota") && args.length >= 4) { // 设置结点配额
            String option = args[1];
            String val = args[2];
            path = args[3];
            System.err.println("Comment: the parts are " +
                               "option " + option +
                               " val " + val +
                               " path " + path);
            if ("-b".equals(option)) {
                // we are setting the bytes quota
                createQuota(zk, path, Long.parseLong(val), -1);
            } else if ("-n".equals(option)) {
                // we are setting the num quota
                createQuota(zk, path, -1L, Integer.parseInt(val));
            } else {
                usage();
            }

        } else if (cmd.equals("delquota") && args.length >= 2) { // 删除结点配额
            //if neither option -n or -b is specified, we delete
            // the quota node for thsi node.
            if (args.length == 3) {
                //this time we have an option
                String option = args[1];
                path = args[2];
                if ("-b".equals(option)) {
                    delQuota(zk, path, true, false);
                } else if ("-n".equals(option)) {
                    delQuota(zk, path, false, true);
                }
            } else if (args.length == 2) {
                path = args[1];
                // we dont have an option specified.
                // just delete whole quota node
                delQuota(zk, path, true, true);
            } else if (cmd.equals("help")) { // 帮助信息
                usage();
            }
        } else if (cmd.equals("close")) { // 关闭连接
                zk.close();
        } else if (cmd.equals("sync") && args.length >= 2) { // 同步
            path = args[1];
            zk.sync(path, new AsyncCallback.VoidCallback() {
                public void processResult(int rc, String path, Object ctx) {
                    System.out.println("Sync returned " + rc);
                }
            }, null );
        } else if (cmd.equals("addauth") && args.length >= 2) { // 增加认证
            byte[] b = null;
            if (args.length >= 3)
                b = args[2].getBytes();

            zk.addAuthInfo(args[1], b);
        } else if (!commandMap.containsKey(cmd)) {
            usage();
        }
        return watch;
    }

    // int转换为权限字符
    private static String getPermString(int perms) {
        StringBuilder p = new StringBuilder();
        if ((perms & ZooDefs.Perms.CREATE) != 0) {
            p.append('c');
        }
        if ((perms & ZooDefs.Perms.DELETE) != 0) {
            p.append('d');
        }
        if ((perms & ZooDefs.Perms.READ) != 0) {
            p.append('r');
        }
        if ((perms & ZooDefs.Perms.WRITE) != 0) {
            p.append('w');
        }
        if ((perms & ZooDefs.Perms.ADMIN) != 0) {
            p.append('a');
        }
        return p.toString();
    }

    // 字符串转换成ACL权限列表
    private static List<ACL> parseACLs(String aclString) {
        List<ACL> acl;
        String acls[] = aclString.split(",");
        acl = new ArrayList<ACL>();
        for (String a : acls) {
            int firstColon = a.indexOf(':');
            int lastColon = a.lastIndexOf(':');
            if (firstColon == -1 || lastColon == -1 || firstColon == lastColon) {
                System.err.println(a + " does not have the form scheme:id:perm");
                continue;
            }
            ACL newAcl = new ACL();
            newAcl.setId(new Id(a.substring(0, firstColon), a.substring(
                    firstColon + 1, lastColon)));
            newAcl.setPerms(getPermFromString(a.substring(lastColon + 1)));
            acl.add(newAcl);
        }
        return acl;
    }
}
