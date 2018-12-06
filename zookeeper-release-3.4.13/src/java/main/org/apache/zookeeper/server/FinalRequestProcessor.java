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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.jute.Record;
import org.apache.zookeeper.common.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.MultiResponse;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.KeeperException.SessionMovedException;
import org.apache.zookeeper.ZooDefs.OpCode;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.proto.CreateResponse;
import org.apache.zookeeper.proto.ExistsRequest;
import org.apache.zookeeper.proto.ExistsResponse;
import org.apache.zookeeper.proto.GetACLRequest;
import org.apache.zookeeper.proto.GetACLResponse;
import org.apache.zookeeper.proto.GetChildren2Request;
import org.apache.zookeeper.proto.GetChildren2Response;
import org.apache.zookeeper.proto.GetChildrenRequest;
import org.apache.zookeeper.proto.GetChildrenResponse;
import org.apache.zookeeper.proto.GetDataRequest;
import org.apache.zookeeper.proto.GetDataResponse;
import org.apache.zookeeper.proto.ReplyHeader;
import org.apache.zookeeper.proto.SetACLResponse;
import org.apache.zookeeper.proto.SetDataResponse;
import org.apache.zookeeper.proto.SetWatches;
import org.apache.zookeeper.proto.SyncRequest;
import org.apache.zookeeper.proto.SyncResponse;
import org.apache.zookeeper.server.DataTree.ProcessTxnResult;
import org.apache.zookeeper.server.ZooKeeperServer.ChangeRecord;
import org.apache.zookeeper.txn.CreateSessionTxn;
import org.apache.zookeeper.txn.ErrorTxn;
import org.apache.zookeeper.txn.TxnHeader;

import org.apache.zookeeper.MultiTransactionRecord;
import org.apache.zookeeper.Op;
import org.apache.zookeeper.OpResult;
import org.apache.zookeeper.OpResult.CheckResult;
import org.apache.zookeeper.OpResult.CreateResult;
import org.apache.zookeeper.OpResult.DeleteResult;
import org.apache.zookeeper.OpResult.SetDataResult;
import org.apache.zookeeper.OpResult.ErrorResult;

/**
 * This Request processor actually applies any transaction associated with a
 * request and services any queries. It is always at the end of a
 * RequestProcessor chain (hence the name), so it does not have a nextProcessor
 * member.
 * 该处理器是最后一个请求处理器，主要用来进行客户端返回之前的收尾工作，包括创建客户端响应，针对事务请求，将事务应用到内存数据库中去
 *
 * This RequestProcessor counts on ZooKeeperServer to populate the
 * outstandingRequests member of ZooKeeperServer.
 */
public class FinalRequestProcessor implements RequestProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(FinalRequestProcessor.class);

    ZooKeeperServer zks; // zookeeper服务

    public FinalRequestProcessor(ZooKeeperServer zks) {
        this.zks = zks;
    }

    // 处理请求
    public void processRequest(Request request) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Processing request:: " + request);
        }
        // request.addRQRec(">final");
        long traceMask = ZooTrace.CLIENT_REQUEST_TRACE_MASK;
        if (request.type == OpCode.ping) { // 服务器ping操作，ping的日志不输出
            traceMask = ZooTrace.SERVER_PING_TRACE_MASK;
        }
        if (LOG.isTraceEnabled()) {
            ZooTrace.logRequest(LOG, traceMask, 'E', request, "");
        }
        ProcessTxnResult rc = null; // 处理结果
        synchronized (zks.outstandingChanges) {
            while (!zks.outstandingChanges.isEmpty()
                    && zks.outstandingChanges.get(0).zxid <= request.zxid) { // 移除未处理队列中请求zxid小于等于request请求zxid的请求
                ChangeRecord cr = zks.outstandingChanges.remove(0); // 从未处理请求队列中移除
                if (cr.zxid < request.zxid) { // 未处理的请求zxid小于request的zxid，输出警告信息
                    LOG.warn("Zxid outstanding "
                            + cr.zxid
                            + " is less than current " + request.zxid);
                }
                if (zks.outstandingChangesForPath.get(cr.path) == cr) {
                    zks.outstandingChangesForPath.remove(cr.path);
                }
            }
            if (request.hdr != null) {
               TxnHeader hdr = request.hdr;
               Record txn = request.txn;

               rc = zks.processTxn(hdr, txn); // 处理事务请求（将请求应用到内存数据库）
            }
            // do not add non quorum packets to the queue.
            if (Request.isQuorum(request.type)) { // 事务请求
                zks.getZKDatabase().addCommittedProposal(request);
            }
        }

        if (request.hdr != null && request.hdr.getType() == OpCode.closeSession) { // 关闭session请求
            ServerCnxnFactory scxn = zks.getServerCnxnFactory();
            // this might be possible since
            // we might just be playing diffs from the leader
            if (scxn != null && request.cnxn == null) {
                // calling this if we have the cnxn results in the client's
                // close session response being lost - we've already closed
                // the session/socket here before we can send the closeSession
                // in the switch block below
                scxn.closeSession(request.sessionId); // 关闭session
                return;
            }
        }

        if (request.cnxn == null) {
            return;
        }
        ServerCnxn cnxn = request.cnxn; // 服务端和客户端连接

        String lastOp = "NA"; // 记录最后的操作
        zks.decInProcess(); // 未处理请求减一
        Code err = Code.OK;
        Record rsp = null;
        boolean closeSession = false;
        try {
            if (request.hdr != null && request.hdr.getType() == OpCode.error) { // error请求
                throw KeeperException.create(KeeperException.Code.get((
                        (ErrorTxn) request.txn).getErr()));
            }

            KeeperException ke = request.getException();
            if (ke != null && request.type != OpCode.multi) { // 请求中有异常（之前的过程可能出现异常设置到request中）抛出异常
                throw ke;
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("{}",request);
            }
            // 针对不同的请求类型进行处理
            switch (request.type) {
                case OpCode.ping: { // 客户端ping
                    zks.serverStats().updateLatency(request.createTime); // 更新服务端统计延时

                    lastOp = "PING";
                    cnxn.updateStatsForResponse(request.cxid, request.zxid, lastOp,
                            request.createTime, Time.currentElapsedTime()); // 更新服务端状态

                    // 响应客户端
                    cnxn.sendResponse(new ReplyHeader(-2,
                            zks.getZKDatabase().getDataTreeLastProcessedZxid(), 0), null, "response");
                    return;
                }
                case OpCode.createSession: { // 创建session
                    zks.serverStats().updateLatency(request.createTime);

                    lastOp = "SESS";
                    cnxn.updateStatsForResponse(request.cxid, request.zxid, lastOp,
                            request.createTime, Time.currentElapsedTime()); // 更新服务端状态（创建session也会更新服务端状态）

                    // 完成session初始化
                    zks.finishSessionInit(request.cnxn, true);
                    return;
                }
                case OpCode.multi: { // 多操作
                    lastOp = "MULT";
                    rsp = new MultiResponse() ;

                    for (ProcessTxnResult subTxnResult : rc.multiResult) { // 将结果存入MultiResponse

                        OpResult subResult ;

                        switch (subTxnResult.type) {
                            case OpCode.check:
                                subResult = new CheckResult();
                                break;
                            case OpCode.create:
                                subResult = new CreateResult(subTxnResult.path);
                                break;
                            case OpCode.delete:
                                subResult = new DeleteResult();
                                break;
                            case OpCode.setData:
                                subResult = new SetDataResult(subTxnResult.stat);
                                break;
                            case OpCode.error:
                                subResult = new ErrorResult(subTxnResult.err) ;
                                break;
                            default:
                                throw new IOException("Invalid type of op");
                        }

                        ((MultiResponse)rsp).add(subResult);
                    }

                    break;
                }
                case OpCode.create: { // 创建结点
                    lastOp = "CREA";
                    rsp = new CreateResponse(rc.path);
                    err = Code.get(rc.err);
                    break;
                }
                case OpCode.delete: { // 删除结点
                    lastOp = "DELE";
                    err = Code.get(rc.err);
                    break;
                }
                case OpCode.setData: { // 更新结点
                    lastOp = "SETD";
                    rsp = new SetDataResponse(rc.stat);
                    err = Code.get(rc.err);
                    break;
                }
                case OpCode.setACL: { // 设置结点访问权限
                    lastOp = "SETA";
                    rsp = new SetACLResponse(rc.stat);
                    err = Code.get(rc.err);
                    break;
                }
                case OpCode.closeSession: { // 关闭session
                    lastOp = "CLOS";
                    closeSession = true;
                    err = Code.get(rc.err);
                    break;
                }
                case OpCode.sync: { // 服务间数据同步
                    lastOp = "SYNC";
                    SyncRequest syncRequest = new SyncRequest();
                    ByteBufferInputStream.byteBuffer2Record(request.request,
                            syncRequest);
                    rsp = new SyncResponse(syncRequest.getPath());
                    break;
                }
                case OpCode.check: { // 校验
                    lastOp = "CHEC";
                    rsp = new SetDataResponse(rc.stat);
                    err = Code.get(rc.err);
                    break;
                }
                case OpCode.exists: { // 结点是否存在
                    lastOp = "EXIS";
                    // TODO we need to figure out the security requirement for this!
                    ExistsRequest existsRequest = new ExistsRequest();
                    ByteBufferInputStream.byteBuffer2Record(request.request,
                            existsRequest);
                    String path = existsRequest.getPath();
                    if (path.indexOf('\0') != -1) {
                        throw new KeeperException.BadArgumentsException();
                    }
                    Stat stat = zks.getZKDatabase().statNode(path, existsRequest
                            .getWatch() ? cnxn : null); // 获取结点stat
                    rsp = new ExistsResponse(stat);
                    break;
                }
                case OpCode.getData: { // 获取结点内容
                    lastOp = "GETD";
                    GetDataRequest getDataRequest = new GetDataRequest();
                    ByteBufferInputStream.byteBuffer2Record(request.request,
                            getDataRequest);
                    DataNode n = zks.getZKDatabase().getNode(getDataRequest.getPath());
                    if (n == null) {
                        throw new KeeperException.NoNodeException();
                    }
                    // 校验结点的读访问权限控制
                    PrepRequestProcessor.checkACL(zks, zks.getZKDatabase().aclForNode(n),
                            ZooDefs.Perms.READ,
                            request.authInfo);
                    Stat stat = new Stat();
                    byte b[] = zks.getZKDatabase().getData(getDataRequest.getPath(), stat,
                            getDataRequest.getWatch() ? cnxn : null); // 从数据库中获取数据stat
                    rsp = new GetDataResponse(b, stat);
                    break;
                }
                case OpCode.setWatches: { // 设置监视器
                    lastOp = "SETW";
                    SetWatches setWatches = new SetWatches();
                    // XXX We really should NOT need this!!!!
                    request.request.rewind();
                    ByteBufferInputStream.byteBuffer2Record(request.request, setWatches);
                    long relativeZxid = setWatches.getRelativeZxid();
                    zks.getZKDatabase().setWatches(relativeZxid,
                            setWatches.getDataWatches(),
                            setWatches.getExistWatches(),
                            setWatches.getChildWatches(), cnxn);// 数据库设置watch
                    break;
                }
                case OpCode.getACL: { // 获取结点ACL权限
                    lastOp = "GETA";
                    GetACLRequest getACLRequest = new GetACLRequest();
                    ByteBufferInputStream.byteBuffer2Record(request.request, getACLRequest);
                    Stat stat = new Stat();
                    List<ACL> acl =
                        zks.getZKDatabase().getACL(getACLRequest.getPath(), stat); // 获取ACL权限
                    rsp = new GetACLResponse(acl, stat);
                    break;
                }
                case OpCode.getChildren: { // 获取子结点
                    lastOp = "GETC";
                    GetChildrenRequest getChildrenRequest = new GetChildrenRequest();
                    ByteBufferInputStream.byteBuffer2Record(request.request, getChildrenRequest);
                    DataNode n = zks.getZKDatabase().getNode(getChildrenRequest.getPath());
                    if (n == null) {
                        throw new KeeperException.NoNodeException();
                    }
                    PrepRequestProcessor.checkACL(zks, zks.getZKDatabase().aclForNode(n),
                            ZooDefs.Perms.READ,
                            request.authInfo);
                    List<String> children = zks.getZKDatabase().getChildren(
                            getChildrenRequest.getPath(), null, getChildrenRequest
                                    .getWatch() ? cnxn : null); // 数据库中获取子结点
                    rsp = new GetChildrenResponse(children);
                    break;
                }
                case OpCode.getChildren2: { // 获取结点状态信息和子结点列表
                    lastOp = "GETC";
                    GetChildren2Request getChildren2Request = new GetChildren2Request();
                    ByteBufferInputStream.byteBuffer2Record(request.request, getChildren2Request);
                    Stat stat = new Stat();
                    DataNode n = zks.getZKDatabase().getNode(getChildren2Request.getPath());
                    if (n == null) {
                        throw new KeeperException.NoNodeException();
                    }
                    PrepRequestProcessor.checkACL(zks, zks.getZKDatabase().aclForNode(n),
                            ZooDefs.Perms.READ,
                            request.authInfo);
                    List<String> children = zks.getZKDatabase().getChildren(
                            getChildren2Request.getPath(), stat, getChildren2Request.getWatch() ? cnxn : null);
                    rsp = new GetChildren2Response(children, stat);
                    break;
                }
            }
        } catch (SessionMovedException e) { // session转移
            // session moved is a connection level error, we need to tear
            // down the connection otw ZOOKEEPER-710 might happen
            // ie client on slow follower starts to renew session, fails
            // before this completes, then tries the fast follower (leader)
            // and is successful, however the initial renew is then 
            // successfully fwd/processed by the leader and as a result
            // the client and leader disagree on where the client is most
            // recently attached (and therefore invalid SESSION MOVED generated)
            cnxn.sendCloseSession();
            return;
        } catch (KeeperException e) {
            err = e.code();
        } catch (Exception e) { // 输出异常错误
            // log at error level as we are returning a marshalling
            // error to the user
            LOG.error("Failed to process " + request, e);
            StringBuilder sb = new StringBuilder();
            ByteBuffer bb = request.request;
            bb.rewind(); // 设置缓冲区position=0，可以从头开始读数据
            while (bb.hasRemaining()) {
                sb.append(Integer.toHexString(bb.get() & 0xff));
            }
            LOG.error("Dumping request buffer: 0x" + sb.toString());
            err = Code.MARSHALLINGERROR;
        }

        long lastZxid = zks.getZKDatabase().getDataTreeLastProcessedZxid(); // 最近zxid
        ReplyHeader hdr =
            new ReplyHeader(request.cxid, lastZxid, err.intValue()); // 响应头

        zks.serverStats().updateLatency(request.createTime); // 更新服务端延时统计
        cnxn.updateStatsForResponse(request.cxid, lastZxid, lastOp,
                    request.createTime, Time.currentElapsedTime()); // 更新服务端状态

        try {
            cnxn.sendResponse(hdr, rsp, "response"); // 响应客户端
            if (closeSession) {
                cnxn.sendCloseSession();
            }
        } catch (IOException e) {
            LOG.error("FIXMSG",e);
        }
    }

    // 关闭请求处理器，因为是最后一个，所以没有信息处理
    public void shutdown() {
        // we are the final link in the chain
        LOG.info("shutdown of request processor complete");
    }

}
