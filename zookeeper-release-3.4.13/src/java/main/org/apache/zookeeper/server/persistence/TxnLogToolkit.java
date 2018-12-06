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

import org.apache.jute.BinaryInputArchive;
import org.apache.jute.BinaryOutputArchive;
import org.apache.jute.Record;
import org.apache.zookeeper.server.TraceFormatter;
import org.apache.zookeeper.server.util.SerializeUtils;
import org.apache.zookeeper.txn.TxnHeader;

import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

import static org.apache.zookeeper.server.persistence.FileTxnLog.TXNLOG_MAGIC; // 导入静态属性
import static org.apache.zookeeper.server.persistence.TxnLogToolkitCliParser.printHelpAndExit; // 导入静态方法

// 事务日志命令工具类（java命令行执行）
public class TxnLogToolkit implements Closeable {

    // 事务日志工具类异常
    static class TxnLogToolkitException extends Exception {
        private static final long serialVersionUID = 1L;
        private int exitCode;

        TxnLogToolkitException(int exitCode, String message, Object... params) {
            super(String.format(message, params));
            this.exitCode = exitCode;
        }

        int getExitCode() {
            return exitCode;
        }
    }

    // 解析参数异常
    static class TxnLogToolkitParseException extends TxnLogToolkitException {
        private static final long serialVersionUID = 1L;

        TxnLogToolkitParseException(int exitCode, String message, Object... params) {
            super(exitCode, message, params);
        }
    }

    private File txnLogFile; // 事务日志文件
    private boolean recoveryMode = false;
    private boolean verbose = false;
    private FileInputStream txnFis;
    private BinaryInputArchive logStream;

    // Recovery mode 恢复模式
    private int crcFixed = 0; // 修复的事务日志记录checksum数目
    private FileOutputStream recoveryFos;
    private BinaryOutputArchive recoveryOa;
    private File recoveryLogFile; // 恢复后的文件
    private FilePadding filePadding = new FilePadding();
    private boolean force = false; // 是否交互方式（询问）修复checksum，默认交互方式

    /**
     * @param args Command line arguments
     */
    public static void main(String[] args) throws Exception {
        final TxnLogToolkit lt = parseCommandLine(args);
        try {
            lt.dump(new Scanner(System.in));
            lt.printStat();
        } catch (TxnLogToolkitParseException e) {
            System.err.println(e.getMessage() + "\n");
            printHelpAndExit(e.getExitCode());
        } catch (TxnLogToolkitException e) {
            System.err.println(e.getMessage());
            System.exit(e.getExitCode()); // 停止程序
        } finally {
            lt.close(); // 关闭文件
        }
    }

    // TxnLogToolkit构造函数
    public TxnLogToolkit(boolean recoveryMode, boolean verbose, String txnLogFileName, boolean force)
            throws FileNotFoundException, TxnLogToolkitException {
        this.recoveryMode = recoveryMode;
        this.verbose = verbose;
        this.force = force;
        txnLogFile = new File(txnLogFileName);
        if (!txnLogFile.exists() || !txnLogFile.canRead()) {
            throw new TxnLogToolkitException(1, "File doesn't exist or not readable: %s", txnLogFile);
        }
        if (recoveryMode) { // 恢复模式
            recoveryLogFile = new File(txnLogFile.toString() + ".fixed"); // 恢复后的文件
            if (recoveryLogFile.exists()) {
                throw new TxnLogToolkitException(1, "Recovery file %s already exists or not writable", recoveryLogFile);
            }
        }

        openTxnLogFile(); // 打开事务日志文件流
        if (recoveryMode) {
            openRecoveryFile();
        }
    }

    // 校验文件信息并输出详细信息，如果是恢复模式，恢复文件
    public void dump(Scanner scanner) throws Exception {
        crcFixed = 0; // 修复的文件记录数

        FileHeader fhdr = new FileHeader();
        fhdr.deserialize(logStream, "fileheader"); // 事务日志文件中读取（反序列化）文件头
        if (fhdr.getMagic() != TXNLOG_MAGIC) { // 魔数不相等
            throw new TxnLogToolkitException(2, "Invalid magic number for %s", txnLogFile.getName());
        }
        // 输出版本信息
        System.out.println("ZooKeeper Transactional Log File with dbid "
                + fhdr.getDbid() + " txnlog format version "
                + fhdr.getVersion());

        if (recoveryMode) { // 恢复模式
            fhdr.serialize(recoveryOa, "fileheader");
            recoveryFos.flush();
            filePadding.setCurrentSize(recoveryFos.getChannel().position());
        }

        int count = 0; // 事务日志记录条数
        while (true) {
            long crcValue; // 文件校验和checksum
            byte[] bytes;
            try {
                crcValue = logStream.readLong("crcvalue");
                bytes = logStream.readBuffer("txnEntry"); // 读取事务
            } catch (EOFException e) {
                System.out.println("EOF reached after " + count + " txns.");
                return;
            }
            if (bytes.length == 0) {
                // Since we preallocate, we define EOF to be an
                // empty transaction
                System.out.println("EOF reached after " + count + " txns.");
                return;
            }
            Checksum crc = new Adler32(); // checksum算法
            crc.update(bytes, 0, bytes.length);
            if (crcValue != crc.getValue()) { // checksum不一致
                if (recoveryMode) {
                    if (!force) {
                        printTxn(bytes, "CRC ERROR");
                        if (askForFix(scanner)) { // 交互方式（询问）确定是否恢复
                            crcValue = crc.getValue();
                            ++crcFixed;
                        }
                    } else {
                        crcValue = crc.getValue();
                        printTxn(bytes, "CRC FIXED");
                        ++crcFixed;
                    }
                } else {
                    printTxn(bytes, "CRC ERROR");
                }
            }
            if (!recoveryMode || verbose) { // 如果是非恢复模式或verbose等于true，输出记录的详细内容
                printTxn(bytes);
            }
            if (logStream.readByte("EOR") != 'B') { // 如果记录不是以B结尾
                throw new TxnLogToolkitException(1, "Last transaction was partial.");
            }
            if (recoveryMode) { // 记录写入恢复文件中
                filePadding.padFile(recoveryFos.getChannel());
                recoveryOa.writeLong(crcValue, "crcvalue");
                recoveryOa.writeBuffer(bytes, "txnEntry");
                recoveryOa.writeByte((byte)'B', "EOR");
            }
            count++;
        }
    }

    // 交互方式确定是否恢复记录checksum
    private boolean askForFix(Scanner scanner) throws TxnLogToolkitException {
        while (true) {
            System.out.print("Would you like to fix it (Yes/No/Abort) ? ");
            char answer = Character.toUpperCase(scanner.next().charAt(0));
            switch (answer) {
                case 'Y': // 恢复
                    return true;
                case 'N': // 不恢复
                    return false;
                case 'A': // 中断
                    throw new TxnLogToolkitException(0, "Recovery aborted.");
            }
        }
    }

    private void printTxn(byte[] bytes) throws IOException {
        printTxn(bytes, "");
    }

    // 输出请求事务信息
    private void printTxn(byte[] bytes, String prefix) throws IOException {
        TxnHeader hdr = new TxnHeader();
        Record txn = SerializeUtils.deserializeTxn(bytes, hdr);
        String txns = String.format("%s session 0x%s cxid 0x%s zxid 0x%s %s %s",
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG).format(new Date(hdr.getTime())),
                Long.toHexString(hdr.getClientId()),
                Long.toHexString(hdr.getCxid()),
                Long.toHexString(hdr.getZxid()),
                TraceFormatter.op2String(hdr.getType()),
                txn);
        if (prefix != null && !"".equals(prefix.trim())) {
            System.out.print(prefix + " - ");
        }
        if (txns.endsWith("\n")) {
            System.out.print(txns);
        } else {
            System.out.println(txns);
        }
    }

    // 打开事务日志文件流
    private void openTxnLogFile() throws FileNotFoundException {
        txnFis = new FileInputStream(txnLogFile);
        logStream = BinaryInputArchive.getArchive(txnFis);
    }

    // 关闭事务日志文件流
    private void closeTxnLogFile() throws IOException {
        if (txnFis != null) {
            txnFis.close();
        }
    }

    // 打开恢复文件流
    private void openRecoveryFile() throws FileNotFoundException {
        recoveryFos = new FileOutputStream(recoveryLogFile);
        recoveryOa = BinaryOutputArchive.getArchive(recoveryFos);
    }

    // 关闭恢复文件流
    private void closeRecoveryFile() throws IOException {
        if (recoveryFos != null) {
            recoveryFos.close();
        }
    }

    // 解析参数，构造TxnLogToolkit
    private static TxnLogToolkit parseCommandLine(String[] args) throws TxnLogToolkitException, FileNotFoundException {
        TxnLogToolkitCliParser parser = new TxnLogToolkitCliParser();
        parser.parse(args);
        return new TxnLogToolkit(parser.isRecoveryMode(), parser.isVerbose(), parser.getTxnLogFileName(), parser.isForce());
    }

    // 输出恢复信息
    private void printStat() {
        if (recoveryMode) {
            System.out.printf("Recovery file %s has been written with %d fixed CRC error(s)%n", recoveryLogFile, crcFixed);
        }
    }

    // 关闭文件
    @Override
    public void close() throws IOException {
        if (recoveryMode) {
            closeRecoveryFile();
        }
        closeTxnLogFile();
    }
}
