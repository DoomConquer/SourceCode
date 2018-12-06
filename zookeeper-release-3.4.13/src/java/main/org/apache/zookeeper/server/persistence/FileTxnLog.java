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
package org.apache.zookeeper.server.persistence;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

import org.apache.jute.BinaryInputArchive;
import org.apache.jute.BinaryOutputArchive;
import org.apache.jute.InputArchive;
import org.apache.jute.OutputArchive;
import org.apache.jute.Record;
import org.apache.zookeeper.server.ServerStats;
import org.apache.zookeeper.server.util.SerializeUtils;
import org.apache.zookeeper.txn.TxnHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the TxnLog interface. It provides api's
 * to access the txnlogs and add entries to it.
 * 事务日志文件处理
 * <p>
 * The format of a Transactional log is as follows:
 * <blockquote><pre>
 * LogFile:
 *     FileHeader TxnList ZeroPad
 * 
 * FileHeader: {
 *     magic 4bytes (ZKLG)
 *     version 4bytes
 *     dbid 8bytes
 *   }
 * 
 * TxnList:
 *     Txn || Txn TxnList
 *     
 * Txn:
 *     checksum Txnlen TxnHeader Record 0x42
 * 
 * checksum: 8bytes Adler32 is currently used
 *   calculated across payload -- Txnlen, TxnHeader, Record and 0x42
 * 
 * Txnlen:
 *     len 4bytes
 * 
 * TxnHeader: {
 *     sessionid 8bytes
 *     cxid 4bytes
 *     zxid 8bytes
 *     time 8bytes
 *     type 4bytes
 *   }
 *     
 * Record:
 *     See Jute definition file for details on the various record types
 *      
 * ZeroPad: 0填充
 *     0 padded to EOF (filled during preallocation stage)
 * </pre></blockquote> 
 */
public class FileTxnLog implements TxnLog {
    private static final Logger LOG;

    public final static int TXNLOG_MAGIC =
        ByteBuffer.wrap("ZKLG".getBytes()).getInt(); // 事务日志文件魔数

    public final static int VERSION = 2; // 事务日志文件版本

    public static final String LOG_FILE_PREFIX = "log"; // 日志文件前缀

    /** Maximum time we allow for elapsed fsync before WARNing */
    private final static long fsyncWarningThresholdMS; // fsync多长时间（毫秒）后警告

    static {
        LOG = LoggerFactory.getLogger(FileTxnLog.class);

        /** Local variable to read fsync.warningthresholdms into */
        Long fsyncWarningThreshold;
        if ((fsyncWarningThreshold = Long.getLong("zookeeper.fsync.warningthresholdms")) == null)
            fsyncWarningThreshold = Long.getLong("fsync.warningthresholdms", 1000); // 获取系统属性fsync.warningthresholdms，默认1000
        fsyncWarningThresholdMS = fsyncWarningThreshold;
    }

    long lastZxidSeen; // 记录写入日志最大zxid
    volatile BufferedOutputStream logStream = null; // 日志buffer输出流
    volatile OutputArchive oa;
    volatile FileOutputStream fos = null; // 日志文件输出流

    File logDir; // 日志文件目录
    private final boolean forceSync = !System.getProperty("zookeeper.forceSync", "yes").equals("no"); // 是否强制同步
    long dbId; // 数据库标识
    private LinkedList<FileOutputStream> streamsToFlush = new LinkedList<FileOutputStream>(); // 等待刷新的文件流队列（为了提高效率，不是每次数据写到流中就flush到磁盘）
    File logFileWrite = null; // 当前日志写入的文件
    private FilePadding filePadding = new FilePadding(); // 文件填充工具类

    private ServerStats serverStats; // 服务端统计信息

    /**
     * constructor for FileTxnLog. Take the directory
     * where the txnlogs are stored
     * @param logDir the directory where the txnlogs are stored
     */
    public FileTxnLog(File logDir) {
        this.logDir = logDir;
    }

    /**
      * method to allow setting preallocate size
      * of log file to pad the file.
      * 设置预分配空间，填充0
     *
      * @param size the size to set to in bytes
      */
    public static void setPreallocSize(long size) {
        FilePadding.setPreallocSize(size);
    }

    /**
     * Setter for ServerStats to monitor fsync threshold exceed
     * 设置ServerStats，用于统计fsync超过次数
     *
     * @param serverStats used to update fsyncThresholdExceedCount
     */
     @Override
     public void setServerStats(ServerStats serverStats) {
         this.serverStats = serverStats;
     }

    /**
     * creates a checksum algorithm to be used 创建checksum算法
     * @return the checksum used for this txnlog
     */
    protected Checksum makeChecksumAlgorithm(){
        return new Adler32();
    }

    /**
     * rollover the current log file to a new one.
     * 使用一个新的文件记录日志
     *
     * @throws IOException
     */
    public synchronized void rollLog() throws IOException {
        if (logStream != null) {
            this.logStream.flush(); // logStream中的数据刷新到FileOutputStream
            this.logStream = null;
            oa = null;
        }
    }

    /**
     * close all the open file handles
     * 关闭事务日志文件（关闭前commit方法确保数据都写到磁盘）
     *
     * @throws IOException
     */
    public synchronized void close() throws IOException {
        if (logStream != null) {
            logStream.close();
        }
        for (FileOutputStream log : streamsToFlush) {
            log.close();
        }
    }

    /**
     * append an entry to the transaction log
     * 追加日志到文件中
     *
     * @param hdr the header of the transaction
     * @param txn the transaction part of the entry
     * returns true iff something appended, otw false 
     */
    public synchronized boolean append(TxnHeader hdr, Record txn)
        throws IOException
    {
        if (hdr == null) {
            return false;
        }

        if (hdr.getZxid() <= lastZxidSeen) {
            LOG.warn("Current zxid " + hdr.getZxid()
                    + " is <= " + lastZxidSeen + " for "
                    + hdr.getType());
        } else {
            lastZxidSeen = hdr.getZxid(); // 记录最近请求的zxid
        }

        if (logStream == null) { // logStream为空，用当前zxid新建日志文件
           if(LOG.isInfoEnabled()){
                LOG.info("Creating new log file: " + Util.makeLogName(hdr.getZxid()));
           }

           logFileWrite = new File(logDir, Util.makeLogName(hdr.getZxid()));
           fos = new FileOutputStream(logFileWrite);
           logStream = new BufferedOutputStream(fos);
           oa = BinaryOutputArchive.getArchive(logStream);
           FileHeader fhdr = new FileHeader(TXNLOG_MAGIC, VERSION, dbId);
           fhdr.serialize(oa, "fileheader");
           // Make sure that the magic number is written before padding.
           logStream.flush(); // 填充前先将魔数刷新到FileOutputStream
           filePadding.setCurrentSize(fos.getChannel().position()); // 设置文件当前大小
           streamsToFlush.add(fos); // 加入等待刷新队列
        }
        filePadding.padFile(fos.getChannel()); // 填充文件（如果空间不足4KB扩容并填充）
        byte[] buf = Util.marshallTxnEntry(hdr, txn); // 序列化事务头和数据到buf中
        if (buf == null || buf.length == 0) {
            throw new IOException("Faulty serialization for header " + "and txn");
        }
        Checksum crc = makeChecksumAlgorithm();
        crc.update(buf, 0, buf.length); // 生成checksum
        oa.writeLong(crc.getValue(), "txnEntryCRC"); // 写入checksum
        Util.writeTxnBytes(oa, buf); // buf数据写入oa流

        return true;
    }

    /**
     * Find the log file that starts at, or just before, the snapshot. Return
     * this and all subsequent logs. Results are ordered by zxid of file,
     * ascending order.
     * 获取zxid大于或等于logZxid的文件（logZxid是小于或等于snapshotZxid中最大的zxid）
     *
     * @param logDirList array of files
     * @param snapshotZxid return files at, or before this zxid
     * @return
     */
    public static File[] getLogFiles(File[] logDirList, long snapshotZxid) {
        List<File> files = Util.sortDataDir(logDirList, LOG_FILE_PREFIX, true);
        long logZxid = 0; // 小于等于snapshotZxid中最大的zxid
        // Find the log file that starts before or at the same time as the
        // zxid of the snapshot
        for (File f : files) {
            long fzxid = Util.getZxidFromName(f.getName(), LOG_FILE_PREFIX);
            if (fzxid > snapshotZxid) {
                continue;
            }
            // the files
            // are sorted with zxid's
            if (fzxid > logZxid) {
                logZxid = fzxid;
            }
        }
        List<File> v = new ArrayList<File>(5);
        for (File f : files) {
            long fzxid = Util.getZxidFromName(f.getName(), LOG_FILE_PREFIX);
            if (fzxid < logZxid) {
                continue;
            }
            v.add(f);
        }
        return v.toArray(new File[0]);

    }

    /**
     * get the last zxid that was logged in the transaction logs
     * 获取最近一次日志记录的zxid（不是文件名称中的zxid，是记录到文件中的最近的zxid）
     *
     * @return the last zxid logged in the transaction logs
     */
    public long getLastLoggedZxid() {
        File[] files = getLogFiles(logDir.listFiles(), 0); // 获取所有事务日志文件
        long maxLog = files.length > 0 ?
                Util.getZxidFromName(files[files.length - 1].getName(), LOG_FILE_PREFIX) : -1; // 文件名称中最近的zxid

        // if a log file is more recent we must scan it to find
        // the highest zxid
        long zxid = maxLog;
        TxnIterator itr = null; // 日志迭代器
        try {
            FileTxnLog txn = new FileTxnLog(logDir);
            itr = txn.read(maxLog); // 获取事务id大于等于maxLog的文件迭代器
            while (true) { // 一直读到文件最后
                if(!itr.next())
                    break;
                TxnHeader hdr = itr.getHeader();
                zxid = hdr.getZxid();
            }
        } catch (IOException e) {
            LOG.warn("Unexpected exception", e);
        } finally {
            close(itr); // 关闭迭代器
        }
        return zxid;
    }

    // 关闭迭代器
    private void close(TxnIterator itr) {
        if (itr != null) {
            try {
                itr.close();
            } catch (IOException ioe) {
                LOG.warn("Error closing file iterator", ioe);
            }
        }
    }

    /**
     * commit the logs. make sure that everything hits the disk
     * 提交日志，将缓存区的数据写到磁盘
     */
    public synchronized void commit() throws IOException {
        if (logStream != null) {
            logStream.flush(); // 将buffer流中数据刷新到FileOutputStream
        }
        for (FileOutputStream log : streamsToFlush) {
            log.flush(); // 文件流中的数据刷新到磁盘（并不保证写到磁盘，流中的数据已经传递给操作系统）
            if (forceSync) { // 强制同步
                long startSyncNS = System.nanoTime();

                log.getChannel().force(false); // 强制数据写到磁盘

                long syncElapsedMS =
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startSyncNS);
                if (syncElapsedMS > fsyncWarningThresholdMS) {
                    if(serverStats != null) { // 统计强制同步超时次数
                        serverStats.incrementFsyncThresholdExceedCount();
                    }
                    LOG.warn("fsync-ing the write ahead log in "
                            + Thread.currentThread().getName()
                            + " took " + syncElapsedMS
                            + "ms which will adversely effect operation latency. "
                            + "See the ZooKeeper troubleshooting guide");
                }
            }
        }
        while (streamsToFlush.size() > 1) { // 移除streamsToFlush队列中待刷新的流
            streamsToFlush.removeFirst().close();
        }
    }

    /**
     * start reading all the transactions from the given zxid
     * 根据给定的zxid获取读取日志的迭代器
     *
     * @param zxid the zxid to start reading transactions from
     * @return returns an iterator to iterate through the transaction
     * logs
     */
    public TxnIterator read(long zxid) throws IOException {
        return new FileTxnIterator(logDir, zxid);
    }

    /**
     * truncate the current transaction logs
     * 截断日志文件（删除zxid后面的记录）
     *
     * @param zxid the zxid to truncate the logs to
     * @return true if successful false if not
     */
    public boolean truncate(long zxid) throws IOException {
        FileTxnIterator itr = null;
        try {
            itr = new FileTxnIterator(this.logDir, zxid);
            PositionInputStream input = itr.inputStream;
            if(input == null) {
                throw new IOException("No log files found to truncate! This could " +
                        "happen if you still have snapshots from an old setup or " +
                        "log files were deleted accidentally or dataLogDir was changed in zoo.cfg.");
            }
            long pos = input.getPosition(); // 文件当前读取到位置
            // now, truncate at the current position
            RandomAccessFile raf = new RandomAccessFile(itr.logFile, "rw");
            raf.setLength(pos); // 设置文件长度，如果文件长度大于pos，文件将被截断，删除后面的内容
            raf.close();
            while (itr.goToNextLog()) { // 删除zxid记录后续的文件
                if (!itr.logFile.delete()) {
                    LOG.warn("Unable to truncate {}", itr.logFile);
                }
            }
        } finally {
            close(itr); // 关闭迭代器
        }
        return true;
    }

    /**
     * read the header of the transaction file
     * 读取事务日志文件头
     *
     * @param file the transaction file to read
     * @return header that was read fomr the file
     * @throws IOException
     */
    private static FileHeader readHeader(File file) throws IOException {
        InputStream is =null;
        try {
            is = new BufferedInputStream(new FileInputStream(file));
            InputArchive ia = BinaryInputArchive.getArchive(is);
            FileHeader hdr = new FileHeader();
            hdr.deserialize(ia, "fileheader");
            return hdr;
         } finally {
             try {
                 if (is != null) is.close();
             } catch (IOException e) {
                 LOG.warn("Ignoring exception during close", e);
             }
         }
    }

    /**
     * the dbid of this transaction database
     * 返回事务（内存）数据库的dbid
     *
     * @return the dbid of this database
     */
    public long getDbId() throws IOException {
        FileTxnIterator itr = new FileTxnIterator(logDir, 0);
        FileHeader fh = readHeader(itr.logFile); // 最下zxid记录文件
        itr.close();
        if(fh == null)
            throw new IOException("Unsupported Format.");
        return fh.getDbid();
    }

    /**
     * the forceSync value. true if forceSync is enabled, false otherwise.
     * @return the forceSync value
     */
    public boolean isForceSync() {
        return forceSync;
    }

    /**
     * a class that keeps track of the position 
     * in the input stream. The position points to offset
     * that has been consumed by the applications. It can 
     * wrap buffered input streams to provide the right offset 
     * for the application.
     * 封装输入流，记录输入流offset位置
     */
    static class PositionInputStream extends FilterInputStream {
        long position; // offset位置
        protected PositionInputStream(InputStream in) {
            super(in);
            position = 0;
        }
        
        @Override
        public int read() throws IOException {
            int rc = super.read();
            if (rc > -1) {
                position++;
            }
            return rc;
        }

        public int read(byte[] b) throws IOException {
            int rc = super.read(b);
            if (rc > 0) {
                position += rc;
            }
            return rc;            
        }
        
        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int rc = super.read(b, off, len);
            if (rc > 0) {
                position += rc;
            }
            return rc;
        }
        
        @Override
        public long skip(long n) throws IOException {
            long rc = super.skip(n);
            if (rc > 0) {
                position += rc;
            }
            return rc;
        }

        // 返回当前偏移位置
        public long getPosition() {
            return position;
        }

        // 不支持mark
        @Override
        public boolean markSupported() {
            return false;
        }

        @Override
        public void mark(int readLimit) {
            throw new UnsupportedOperationException("mark");
        }

        @Override
        public void reset() {
            throw new UnsupportedOperationException("reset");
        }
    }
    
    /**
     * this class implements the txnlog iterator interface
     * which is used for reading the transaction logs
     * 日志迭代器
     */
    public static class FileTxnIterator implements TxnLog.TxnIterator {
        File logDir;
        long zxid;       // 事务id，从该事务id开始迭代
        TxnHeader hdr;   // 记录当前迭代的事务头
        Record record;   // 当前迭代的记录
        File logFile;    // 当前迭代的日志文件
        InputArchive ia; // 当前迭代的InputArchive
        static final String CRC_ERROR = "CRC check failed"; // checksum校验失败信息
       
        PositionInputStream inputStream = null;

        //stored files is the list of files greater than
        //the zxid we are looking for.
        private ArrayList<File> storedFiles; // 保存所有事务id大于等于zxid的文件和最近一次事务id小于zxid的文件

        /**
         * create an iterator over a transaction database directory
         * @param logDir the transaction database directory
         * @param zxid the zxid to start reading from
         * @throws IOException
         */
        public FileTxnIterator(File logDir, long zxid) throws IOException {
          this.logDir = logDir;
          this.zxid = zxid;
          init(); // 初始化
        }

        /**
         * initialize to the zxid specified
         * this is inclusive of the zxid
         * 初始化迭代器，让迭代器从zxid记录开始
         *
         * @throws IOException
         */
        void init() throws IOException {
            storedFiles = new ArrayList<File>();
            // 倒序排序所有事务日志文件
            List<File> files = Util.sortDataDir(FileTxnLog.getLogFiles(logDir.listFiles(), 0), LOG_FILE_PREFIX, false);
            for (File f: files) {
                if (Util.getZxidFromName(f.getName(), LOG_FILE_PREFIX) >= zxid) { // 保存事务id大于或等于zxid的文件
                    storedFiles.add(f);
                }
                // add the last logfile that is less than the zxid
                // 保存第一个事务id小于zxid的文件（zxid已经倒序）
                else if (Util.getZxidFromName(f.getName(), LOG_FILE_PREFIX) < zxid) {
                    storedFiles.add(f);
                    break;
                }
            }
            goToNextLog(); // zxid从小到大读取文件
            if (!next())
                return;
            while (hdr.getZxid() < zxid) { // 一直迭代找到第一个事务id大于或等于zxid的记录
                if (!next())
                    return;
            }
        }

        /**
         * go to the next logfile 读取下一个文件（zxid从小到大读取）
         * @return true if there is one and false if there is no
         * new file to be read
         * @throws IOException
         */
        private boolean goToNextLog() throws IOException {
            if (storedFiles.size() > 0) { // zxid从小到大读取
                this.logFile = storedFiles.remove(storedFiles.size() - 1);
                ia = createInputArchive(this.logFile);
                return true;
            }
            return false;
        }

        /**
         * read the header from the inputarchive
         * 读取文件头，验证文件是否合法
         *
         * @param ia the inputarchive to be read from
         * @param is the inputstream
         * @throws IOException
         */
        protected void inStreamCreated(InputArchive ia, InputStream is)
            throws IOException{
            FileHeader header= new FileHeader(); // 文件头
            header.deserialize(ia, "fileheader");
            if (header.getMagic() != FileTxnLog.TXNLOG_MAGIC) { // 魔数不相等
                throw new IOException("Transaction log: " + this.logFile + " has invalid magic number " 
                        + header.getMagic()
                        + " != " + FileTxnLog.TXNLOG_MAGIC);
            }
        }

        /**
         * Invoked to indicate that the input stream has been created.
         * 创建输入流
         *
         * @param logFile file associated with the input archive.
         * @throws IOException
         **/
        protected InputArchive createInputArchive(File logFile) throws IOException {
            if(inputStream == null){
                inputStream = new PositionInputStream(new BufferedInputStream(new FileInputStream(logFile)));
                LOG.debug("Created new input stream " + logFile);
                ia = BinaryInputArchive.getArchive(inputStream);
                inStreamCreated(ia, inputStream); // 文件是否合法
                LOG.debug("Created new input archive " + logFile);
            }
            return ia;
        }

        /**
         * create a checksum algorithm 获取checksum算法
         * @return the checksum algorithm
         */
        protected Checksum makeChecksumAlgorithm(){
            return new Adler32();
        }

        /**
         * the iterator that moves to the next transaction
         * 读取下一条事务日志记录（一个文件中很多记录）
         *
         * @return true if there is more transactions to be read
         * false if not.
         */
        public boolean next() throws IOException {
            if (ia == null) {
                return false;
            }
            try {
                long crcValue = ia.readLong("crcvalue");
                byte[] bytes = Util.readTxnBytes(ia); // 从InputArchive读取事务日志
                // Since we preallocate, we define EOF to be an empty transaction
                if (bytes == null || bytes.length == 0) { // 文件读取结束
                    throw new EOFException("Failed to read " + logFile);
                }
                // EOF or corrupted record
                // validate CRC
                Checksum crc = makeChecksumAlgorithm();
                crc.update(bytes, 0, bytes.length);
                if (crcValue != crc.getValue()) // 校验checksum
                    throw new IOException(CRC_ERROR);
                hdr = new TxnHeader();
                record = SerializeUtils.deserializeTxn(bytes, hdr); // 读取record
            } catch (EOFException e) { // 文件读取结束，读取下一个文件（goToNextLog）
                LOG.debug("EOF excepton " + e);
                inputStream.close();
                inputStream = null;
                ia = null;
                hdr = null;
                // this means that the file has ended
                // we should go to the next file
                if (!goToNextLog()) { // 已经没有文件了
                    return false;
                }
                // if we went to the next log file, we should call next() again
                return next();
            } catch (IOException e) { // IO异常关闭流
                inputStream.close();
                throw e;
            }
            return true;
        }

        /**
         * reutrn the current header 获取当前事务日志头
         * @return the current header that
         * is read
         */
        public TxnHeader getHeader() {
            return hdr;
        }

        /**
         * return the current transaction 获取当前事务日志记录
         * @return the current transaction
         * that is read
         */
        public Record getTxn() {
            return record;
        }

        /**
         * close the iterator
         * and release the resources.
         * 关闭迭代器，释放资源
         */
        public void close() throws IOException {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

}
