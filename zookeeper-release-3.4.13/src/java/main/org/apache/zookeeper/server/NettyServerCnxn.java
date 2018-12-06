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

package org.apache.zookeeper.server;

import static org.jboss.netty.buffer.ChannelBuffers.wrappedBuffer;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.AbstractSet;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jute.BinaryInputArchive;
import org.apache.jute.BinaryOutputArchive;
import org.apache.jute.Record;
import org.apache.zookeeper.server.quorum.ProposalStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.zookeeper.Environment;
import org.apache.zookeeper.Version;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.proto.ReplyHeader;
import org.apache.zookeeper.proto.WatcherEvent;
import org.apache.zookeeper.server.quorum.Leader;
import org.apache.zookeeper.server.quorum.LeaderZooKeeperServer;
import org.apache.zookeeper.server.quorum.ReadOnlyZooKeeperServer;
import org.apache.zookeeper.server.util.OSMXBean;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.MessageEvent;

// netty连接
public class NettyServerCnxn extends ServerCnxn {
    Logger LOG = LoggerFactory.getLogger(NettyServerCnxn.class);
    Channel channel;
    ChannelBuffer queuedBuffer; // 接收数据队列buffer
    volatile boolean throttled; // 接收数据开关，throttled = true不允许接收数据
    ByteBuffer bb; // 接收数据缓冲区
    ByteBuffer bbLen = ByteBuffer.allocate(4); // 接收数据长度缓冲区（4字节）
    long sessionId;
    int sessionTimeout;
    AtomicLong outstandingCount = new AtomicLong(); // 未处理请求数

    /** The ZooKeeperServer for this connection. May be null if the server
     * is not currently serving requests (for example if the server is not
     * an active quorum participant.
     */
    private volatile ZooKeeperServer zkServer;

    NettyServerCnxnFactory factory;
    boolean initialized; // 是否已经完成连接
    
    NettyServerCnxn(Channel channel, ZooKeeperServer zks, NettyServerCnxnFactory factory) {
        this.channel = channel;
        this.zkServer = zks;
        this.factory = factory;
        if (this.factory.login != null) { // sasl登录
            this.zooKeeperSaslServer = new ZooKeeperSaslServer(factory.login);
        }
    }

    // 关闭
    @Override
    public void close() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("close called for sessionid:0x"
                    + Long.toHexString(sessionId));
        }

        // ZOOKEEPER-2743:
        // Always unregister connection upon close to prevent
        // connection bean leak under certain race conditions.
        factory.unregisterConnection(this); // 服务端连接工厂注销该连接

        factory.removeCnxn(this);

        if (channel.isOpen()) {
            channel.close();
        }
    }

    @Override
    public long getSessionId() {
        return sessionId;
    }

    @Override
    public int getSessionTimeout() {
        return sessionTimeout;
    }

    // 处理watch事件
    @Override
    public void process(WatchedEvent event) {
        ReplyHeader h = new ReplyHeader(-1, -1L, 0);
        if (LOG.isTraceEnabled()) {
            ZooTrace.logTraceMessage(LOG, ZooTrace.EVENT_DELIVERY_TRACE_MASK,
                                     "Deliver event " + event + " to 0x"
                                     + Long.toHexString(this.sessionId)
                                     + " through " + this);
        }

        // Convert WatchedEvent to a type that can be sent over the wire
        WatcherEvent e = event.getWrapper();

        try {
            sendResponse(h, e, "notification"); // 发送事件
        } catch (IOException e1) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Problem sending to " + getRemoteSocketAddress(), e1);
            }
            close();
        }
    }

    // 数据长度字节数组
    private static final byte[] fourBytes = new byte[4];

    // 标识一个可以重新读取数据（throttled = false）的事件，调用enableRecv时发送到channel上游进行处理，上游接收到数据后重新设置channel可读（setReadable）
    static class ResumeMessageEvent implements MessageEvent {
        Channel channel;
        ResumeMessageEvent(Channel channel) {
            this.channel = channel;
        }
        @Override
        public Object getMessage() {return null;}
        @Override
        public SocketAddress getRemoteAddress() {return null;}
        @Override
        public Channel getChannel() {return channel;}
        @Override
        public ChannelFuture getFuture() {return null;}
    };

    // 发送响应数据
    @Override
    public void sendResponse(ReplyHeader h, Record r, String tag)
            throws IOException {
        if (!channel.isOpen()) {
            return;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Make space for length
        BinaryOutputArchive bos = BinaryOutputArchive.getArchive(baos);
        try {
            baos.write(fourBytes);
            bos.writeRecord(h, "header");
            if (r != null) {
                bos.writeRecord(r, tag); // 序列化r到bos流中
            }
            baos.close();
        } catch (IOException e) {
            LOG.error("Error serializing response");
        }
        byte b[] = baos.toByteArray();
        ByteBuffer bb = ByteBuffer.wrap(b);
        bb.putInt(b.length - 4).rewind();
        sendBuffer(bb); // 发送数据
        if (h.getXid() > 0) {
            // zks cannot be null otherwise we would not have gotten here!
            if (!zkServer.shouldThrottle(outstandingCount.decrementAndGet())) { // 如果请求堆积没有到限制数量，允许接收数据
                enableRecv();
            }
        }
    }

    @Override
    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
        factory.addSession(sessionId, this);
    }

    // 允许接收数据
    @Override
    public void enableRecv() {
        if (throttled) { // 当前不允许接收数据
            throttled = false;
            if (LOG.isDebugEnabled()) {
                LOG.debug("Sending unthrottle event " + this);
            }
            // 发送ResumeMessageEvent事件到通道的Upstream，上游handler开始处理这个事件
            channel.getPipeline().sendUpstream(new ResumeMessageEvent(channel));
        }
    }

    // 发送数据
    @Override
    public void sendBuffer(ByteBuffer sendBuffer) {
        if (sendBuffer == ServerCnxnFactory.closeConn) {
            close();
            return;
        }
        channel.write(wrappedBuffer(sendBuffer));
        packetSent(); // 统计发送数据包
    }

    @Override
    public InetAddress getSocketAddress() {
        if (channel == null) {
            return null;
        }

        return ((InetSocketAddress)(channel.getRemoteAddress())).getAddress();
    }

    /**
     * clean up the socket related to a command and also make sure we flush the
     * data before we do that
     * 清空处理四字命令时PrintWriter中数据
     * 
     * @param pwriter
     *            the pwriter for a command socket
     */
    private void cleanupWriterSocket(PrintWriter pwriter) {
        try {
            if (pwriter != null) {
                pwriter.flush();
                pwriter.close();
            }
        } catch (Exception e) {
            LOG.info("Error closing PrintWriter ", e);
        } finally {
            try {
                close();
            } catch (Exception e) {
                LOG.error("Error closing a command socket ", e);
            }
        }
    }

    /**
     * This class wraps the sendBuffer method of NIOServerCnxn. It is
     * responsible for chunking up the response to a client. Rather
     * than cons'ing up a response fully in memory, which may be large
     * for some commands, this class chunks up the result.
     */
    private class SendBufferWriter extends Writer {
        private StringBuffer sb = new StringBuffer();
        
        /**
         * Check if we are ready to send another chunk.
         * @param force force sending, even if not a full chunk
         */
        private void checkFlush(boolean force) {
            if ((force && sb.length() > 0) || sb.length() > 2048) {
                sendBuffer(ByteBuffer.wrap(sb.toString().getBytes()));
                // clear our internal buffer
                sb.setLength(0);
            }
        }

        @Override
        public void close() throws IOException {
            if (sb == null) return;
            checkFlush(true);
            sb = null; // clear out the ref to ensure no reuse
        }

        @Override
        public void flush() throws IOException {
            checkFlush(true);
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            sb.append(cbuf, off, len);
            checkFlush(false);
        }
    }

    private static final String ZK_NOT_SERVING =
        "This ZooKeeper instance is not currently serving requests";
    
    /**
     * Set of threads for commmand ports. All the 4
     * letter commands are run via a thread. Each class
     * maps to a correspoding 4 letter command. CommandThread
     * is the abstract class from which all the others inherit.
     * 下面四字命令处理和NIO方式一样
     */
    private abstract class CommandThread /*extends Thread*/ {
        PrintWriter pw;
        
        CommandThread(PrintWriter pw) {
            this.pw = pw;
        }
        
        public void start() {
            run();
        }

        public void run() {
            try {
                commandRun();
            } catch (IOException ie) {
                LOG.error("Error in running command ", ie);
            } finally {
                cleanupWriterSocket(pw);
            }
        }
        
        public abstract void commandRun() throws IOException;
    }
    
    private class RuokCommand extends CommandThread {
        public RuokCommand(PrintWriter pw) {
            super(pw);
        }
        
        @Override
        public void commandRun() {
            pw.print("imok");
            
        }
    }
    
    private class TraceMaskCommand extends CommandThread {
        TraceMaskCommand(PrintWriter pw) {
            super(pw);
        }
        
        @Override
        public void commandRun() {
            long traceMask = ZooTrace.getTextTraceLevel();
            pw.print(traceMask);
        }
    }
    
    private class SetTraceMaskCommand extends CommandThread {
        long trace = 0;
        SetTraceMaskCommand(PrintWriter pw, long trace) {
            super(pw);
            this.trace = trace;
        }
        
        @Override
        public void commandRun() {
            pw.print(trace);
        }
    }
    
    private class EnvCommand extends CommandThread {
        EnvCommand(PrintWriter pw) {
            super(pw);
        }
        
        @Override
        public void commandRun() {
            List<Environment.Entry> env = Environment.list();

            pw.println("Environment:");
            for(Environment.Entry e : env) {
                pw.print(e.getKey());
                pw.print("=");
                pw.println(e.getValue());
            }
            
        } 
    }
    
    private class ConfCommand extends CommandThread {
        ConfCommand(PrintWriter pw) {
            super(pw);
        }
            
        @Override
        public void commandRun() {
            if (!isZKServerRunning()) {
                pw.println(ZK_NOT_SERVING);
            } else {
                zkServer.dumpConf(pw);
            }
        }
    }
    
    private class StatResetCommand extends CommandThread {
        public StatResetCommand(PrintWriter pw) {
            super(pw);
        }
        
        @Override
        public void commandRun() {
            if (!isZKServerRunning()) {
                pw.println(ZK_NOT_SERVING);
            }
            else {
                ServerStats serverStats = zkServer.serverStats();
                serverStats.reset();
                if (serverStats.getServerState().equals("leader")) {
                    ((LeaderZooKeeperServer)zkServer).getLeader().getProposalStats().reset();
                }
                pw.println("Server stats reset.");
            }
        }
    }
    
    private class CnxnStatResetCommand extends CommandThread {
        public CnxnStatResetCommand(PrintWriter pw) {
            super(pw);
        }
        
        @Override
        public void commandRun() {
            if (!isZKServerRunning()) {
                pw.println(ZK_NOT_SERVING);
            } else {
                synchronized(factory.cnxns){
                    for(ServerCnxn c : factory.cnxns){
                        c.resetStats();
                    }
                }
                pw.println("Connection stats reset.");
            }
        }
    }

    private class DumpCommand extends CommandThread {
        public DumpCommand(PrintWriter pw) {
            super(pw);
        }
        
        @Override
        public void commandRun() {
            if (!isZKServerRunning()) {
                pw.println(ZK_NOT_SERVING);
            }
            else {
                pw.println("SessionTracker dump:");
                zkServer.sessionTracker.dumpSessions(pw);
                pw.println("ephemeral nodes dump:");
                zkServer.dumpEphemerals(pw);
            }
        }
    }
    
    private class StatCommand extends CommandThread {
        int len;
        public StatCommand(PrintWriter pw, int len) {
            super(pw);
            this.len = len;
        }
        
        @Override
        public void commandRun() {
            if (!isZKServerRunning()) {
                pw.println(ZK_NOT_SERVING);
            }
            else {   
                pw.print("Zookeeper version: ");
                pw.println(Version.getFullVersion());
                if (zkServer instanceof ReadOnlyZooKeeperServer) {
                    pw.println("READ-ONLY mode; serving only " + "read-only clients");
                }
                if (len == statCmd) {
                    LOG.info("Stat command output");
                    pw.println("Clients:");
                    // clone should be faster than iteration
                    // ie give up the cnxns lock faster
                    HashSet<ServerCnxn> cnxns;
                    synchronized(factory.cnxns){
                        cnxns = new HashSet<ServerCnxn>(factory.cnxns);
                    }
                    for(ServerCnxn c : cnxns){
                        c.dumpConnectionInfo(pw, true);
                        pw.println();
                    }
                    pw.println();
                }
                ServerStats serverStats = zkServer.serverStats();
                pw.print(serverStats.toString());
                pw.print("Node count: ");
                pw.println(zkServer.getZKDatabase().getNodeCount());
                if (serverStats.getServerState().equals("leader")) {
                    Leader leader = ((LeaderZooKeeperServer)zkServer).getLeader();
                    ProposalStats proposalStats = leader.getProposalStats();
                    pw.printf("Proposal sizes last/min/max: %s%n", proposalStats.toString());
                }
            }
        }
    }
    
    private class ConsCommand extends CommandThread {
        public ConsCommand(PrintWriter pw) {
            super(pw);
        }
        
        @Override
        public void commandRun() {
            if (!isZKServerRunning()) {
                pw.println(ZK_NOT_SERVING);
            } else {
                // clone should be faster than iteration
                // ie give up the cnxns lock faster
                AbstractSet<ServerCnxn> cnxns;
                synchronized (factory.cnxns) {
                    cnxns = new HashSet<ServerCnxn>(factory.cnxns);
                }
                for (ServerCnxn c : cnxns) {
                    c.dumpConnectionInfo(pw, false);
                    pw.println();
                }
                pw.println();
            }
        }
    }
    
    private class WatchCommand extends CommandThread {
        int len = 0;
        public WatchCommand(PrintWriter pw, int len) {
            super(pw);
            this.len = len;
        }

        @Override
        public void commandRun() {
            if (!isZKServerRunning()) {
                pw.println(ZK_NOT_SERVING);
            } else {
                DataTree dt = zkServer.getZKDatabase().getDataTree();
                if (len == wchsCmd) {
                    dt.dumpWatchesSummary(pw);
                } else if (len == wchpCmd) {
                    dt.dumpWatches(pw, true);
                } else {
                    dt.dumpWatches(pw, false);
                }
                pw.println();
            }
        }
    }

    private class MonitorCommand extends CommandThread {

        MonitorCommand(PrintWriter pw) {
            super(pw);
        }

        @Override
        public void commandRun() {
            if(!isZKServerRunning()) {
                pw.println(ZK_NOT_SERVING);
                return;
            }
            ZKDatabase zkdb = zkServer.getZKDatabase();
            ServerStats stats = zkServer.serverStats();

            print("version", Version.getFullVersion());

            print("avg_latency", stats.getAvgLatency());
            print("max_latency", stats.getMaxLatency());
            print("min_latency", stats.getMinLatency());

            print("packets_received", stats.getPacketsReceived());
            print("packets_sent", stats.getPacketsSent());
            print("num_alive_connections", stats.getNumAliveClientConnections());

            print("outstanding_requests", stats.getOutstandingRequests());

            print("server_state", stats.getServerState());
            print("znode_count", zkdb.getNodeCount());

            print("watch_count", zkdb.getDataTree().getWatchCount());
            print("ephemerals_count", zkdb.getDataTree().getEphemeralsCount());
            print("approximate_data_size", zkdb.getDataTree().approximateDataSize());

            OSMXBean osMbean = new OSMXBean();
            if (osMbean != null && osMbean.getUnix() == true) {
                print("open_file_descriptor_count", osMbean.getOpenFileDescriptorCount());
                print("max_file_descriptor_count", osMbean.getMaxFileDescriptorCount());
            }

            print("fsync_threshold_exceed_count", stats.getFsyncThresholdExceedCount());

            if(stats.getServerState().equals("leader")) {
                Leader leader = ((LeaderZooKeeperServer)zkServer).getLeader();

                print("followers", leader.getLearners().size());
                print("synced_followers", leader.getForwardingFollowers().size());
                print("pending_syncs", leader.getNumPendingSyncs());

                print("last_proposal_size", leader.getProposalStats().getLastProposalSize());
                print("max_proposal_size", leader.getProposalStats().getMaxProposalSize());
                print("min_proposal_size", leader.getProposalStats().getMinProposalSize());
            }
        }

        private void print(String key, long number) {
            print(key, "" + number);
        }

        private void print(String key, String value) {
            pw.print("zk_");
            pw.print(key);
            pw.print("\t");
            pw.println(value);
        }

    }

    private class IsroCommand extends CommandThread {

        public IsroCommand(PrintWriter pw) {
            super(pw);
        }

        @Override
        public void commandRun() {
            if (!isZKServerRunning()) {
                pw.print("null");
            } else if (zkServer instanceof ReadOnlyZooKeeperServer) {
                pw.print("ro");
            } else {
                pw.print("rw");
            }
        }
    }

    private class NopCommand extends CommandThread {
        private String msg;

        public NopCommand(PrintWriter pw, String msg) {
            super(pw);
            this.msg = msg;
        }

        @Override
        public void commandRun() {
            pw.println(msg);
        }
    }

    /** Return if four letter word found and responded to, otw false **/
    // 处理四字命令
    private boolean checkFourLetterWord(final Channel channel,
            ChannelBuffer message, final int len) throws IOException
    {
        // We take advantage of the limited size of the length to look
        // for cmds. They are all 4-bytes which fits inside of an int
        if (!ServerCnxn.isKnown(len)) {
            return false;
        }

        channel.setInterestOps(0).awaitUninterruptibly();
        packetReceived();

        final PrintWriter pwriter = new PrintWriter(
                new BufferedWriter(new SendBufferWriter()));

        String cmd = ServerCnxn.getCommandString(len);
        // ZOOKEEPER-2693: don't execute 4lw if it's not enabled.
        if (!ServerCnxn.isEnabled(cmd)) { // 命令白名单
            LOG.debug("Command {} is not executed because it is not in the whitelist.", cmd);
            NopCommand nopCmd = new NopCommand(pwriter, cmd + " is not executed because it is not in the whitelist.");
            nopCmd.start();
            return true;
        }

        LOG.info("Processing " + cmd + " command from " + channel.getRemoteAddress());

        if (len == ruokCmd) {
            RuokCommand ruok = new RuokCommand(pwriter);
            ruok.start();
            return true;
        } else if (len == getTraceMaskCmd) {
            TraceMaskCommand tmask = new TraceMaskCommand(pwriter);
            tmask.start();
            return true;
        } else if (len == setTraceMaskCmd) {
            ByteBuffer mask = ByteBuffer.allocate(8);
            message.readBytes(mask);
            mask.flip(); // 转为读模式
            long traceMask = mask.getLong();
            ZooTrace.setTextTraceLevel(traceMask);
            SetTraceMaskCommand setMask = new SetTraceMaskCommand(pwriter, traceMask);
            setMask.start();
            return true;
        } else if (len == enviCmd) {
            EnvCommand env = new EnvCommand(pwriter);
            env.start();
            return true;
        } else if (len == confCmd) {
            ConfCommand ccmd = new ConfCommand(pwriter);
            ccmd.start();
            return true;
        } else if (len == srstCmd) {
            StatResetCommand strst = new StatResetCommand(pwriter);
            strst.start();
            return true;
        } else if (len == crstCmd) {
            CnxnStatResetCommand crst = new CnxnStatResetCommand(pwriter);
            crst.start();
            return true;
        } else if (len == dumpCmd) {
            DumpCommand dump = new DumpCommand(pwriter);
            dump.start();
            return true;
        } else if (len == statCmd || len == srvrCmd) {
            StatCommand stat = new StatCommand(pwriter, len);
            stat.start();
            return true;
        } else if (len == consCmd) {
            ConsCommand cons = new ConsCommand(pwriter);
            cons.start();
            return true;
        } else if (len == wchpCmd || len == wchcCmd || len == wchsCmd) {
            WatchCommand wcmd = new WatchCommand(pwriter, len);
            wcmd.start();
            return true;
        } else if (len == mntrCmd) {
            MonitorCommand mntr = new MonitorCommand(pwriter);
            mntr.start();
            return true;
        } else if (len == isroCmd) {
            IsroCommand isro = new IsroCommand(pwriter);
            isro.start();
            return true;
        }
        return false;
    }

    // 接收数据进行处理
    public void receiveMessage(ChannelBuffer message) {
        try {
            while(message.readable() && !throttled) { // 缓冲ChannelBuffer中有数据且允许接收数据
                if (bb != null) { // 需要将之前接收的数据和现在的合并
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("message readable " + message.readableBytes()
                                + " bb len " + bb.remaining() + " " + bb);
                        ByteBuffer dat = bb.duplicate(); // 复制缓冲区，提供一个上层视图，底层共享数据
                        dat.flip(); // 读模式
                        LOG.trace(Long.toHexString(sessionId)
                                + " bb 0x"
                                + ChannelBuffers.hexDump(
                                        ChannelBuffers.copiedBuffer(dat)));
                    }

                    if (bb.remaining() > message.readableBytes()) { // 设置bb的limit，用于下面读取message中的数据
                        int newLimit = bb.position() + message.readableBytes();
                        bb.limit(newLimit);
                    }
                    message.readBytes(bb); // 读取bb长度（limit - position）数据
                    bb.limit(bb.capacity()); // 重置bb缓冲区limit

                    if (LOG.isTraceEnabled()) {
                        LOG.trace("after readBytes message readable "
                                + message.readableBytes()
                                + " bb len " + bb.remaining() + " " + bb);
                        ByteBuffer dat = bb.duplicate();
                        dat.flip();
                        LOG.trace("after readbytes "
                                + Long.toHexString(sessionId)
                                + " bb 0x"
                                + ChannelBuffers.hexDump(
                                        ChannelBuffers.copiedBuffer(dat)));
                    }
                    if (bb.remaining() == 0) { // 缓冲区写满了数据，如果没有写满等待下次读取数据
                        packetReceived();
                        bb.flip();

                        ZooKeeperServer zks = this.zkServer;
                        if (zks == null || !zks.isRunning()) { // 检测zkServer服务是否关闭
                            throw new IOException("ZK down");
                        }
                        if (initialized) { // 已经完成连接
                            zks.processPacket(this, bb); // 处理数据包

                            // 如果接收数据包超过（最大堆积请求数）限制，不允许再接收数据
                            if (zks.shouldThrottle(outstandingCount.incrementAndGet())) {
                                disableRecvNoWait();
                            }
                        } else { // 连接数据
                            LOG.debug("got conn req request from "
                                    + getRemoteSocketAddress());
                            zks.processConnectRequest(this, bb); // 处理连接数据包
                            initialized = true;
                        }
                        bb = null; // 每次处理完将bb设置成空，下次接收数据时根据bb是否为空判断是否是新请求数据
                    }
                } else { // 新的请求数据
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("message readable "
                                + message.readableBytes()
                                + " bblenrem " + bbLen.remaining());
                        ByteBuffer dat = bbLen.duplicate();
                        dat.flip();
                        LOG.trace(Long.toHexString(sessionId)
                                + " bbLen 0x"
                                + ChannelBuffers.hexDump(
                                        ChannelBuffers.copiedBuffer(dat)));
                    }

                    if (message.readableBytes() < bbLen.remaining()) { // message可读数据小于接收长度字节缓冲区大小
                        bbLen.limit(bbLen.position() + message.readableBytes());
                    }
                    message.readBytes(bbLen);
                    bbLen.limit(bbLen.capacity());
                    if (bbLen.remaining() == 0) { // 读取完整的长度数据（4字节）
                        bbLen.flip();

                        if (LOG.isTraceEnabled()) {
                            LOG.trace(Long.toHexString(sessionId)
                                    + " bbLen 0x"
                                    + ChannelBuffers.hexDump(
                                            ChannelBuffers.copiedBuffer(bbLen)));
                        }
                        int len = bbLen.getInt(); // 获取数据长度
                        if (LOG.isTraceEnabled()) {
                            LOG.trace(Long.toHexString(sessionId)
                                    + " bbLen len is " + len);
                        }

                        bbLen.clear(); // 清空数据，下一次使用
                        if (!initialized) {
                            if (checkFourLetterWord(channel, message, len)) { // 处理四字命令
                                return;
                            }
                        }
                        if (len < 0 || len > BinaryInputArchive.maxBuffer) { // 长度超过buffer限制，抛出异常
                            throw new IOException("Len error " + len);
                        }
                        bb = ByteBuffer.allocate(len); // 为数据缓冲区分配空间
                    }
                }
            }
        } catch(IOException e) {
            LOG.warn("Closing connection to " + getRemoteSocketAddress(), e);
            close(); // 异常时关闭
        }
    }

    // 不允许接收数据
    @Override
    public void disableRecv() {
        disableRecvNoWait().awaitUninterruptibly();
    }
    
    private ChannelFuture disableRecvNoWait() {
        throttled = true;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Throttling - disabling recv " + this);
        }
        return channel.setReadable(false); // 设置通道不能读数据
    }
    
    @Override
    public long getOutstandingRequests() {
        return outstandingCount.longValue();
    }

    @Override
    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    @Override
    public int getInterestOps() {
        return channel.getInterestOps();
    }

    @Override
    public InetSocketAddress getRemoteSocketAddress() {
        return (InetSocketAddress)channel.getRemoteAddress();
    }

    /** Send close connection packet to the client.
     */
    @Override
    public void sendCloseSession() {
        sendBuffer(ServerCnxnFactory.closeConn);
    }

    @Override
    protected ServerStats serverStats() {
        if (!isZKServerRunning()) {
            return null;
        }
        return zkServer.serverStats();
    }

    /**
     * @return true if the server is running, false otherwise.
     */
    boolean isZKServerRunning() {
        return zkServer != null && zkServer.isRunning();
    }
}
