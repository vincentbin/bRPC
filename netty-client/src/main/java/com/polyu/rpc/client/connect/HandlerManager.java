package com.polyu.rpc.client.connect;

import com.polyu.rpc.client.netty.handler.RpcClientHandler;
import com.polyu.rpc.info.RpcMetaData;
import com.polyu.rpc.route.RpcLoadBalance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class HandlerManager {
    private static final Logger logger = LoggerFactory.getLogger(HandlerManager.class);

    private static ReentrantLock lock = new ReentrantLock();
    private static Condition condition = lock.newCondition();
    /**
     * 当没有可用handler时 重试时间间隔
     */
    private static final long HANDLER_RETRY_TIME_INTERVAL = 5000L;

    /**
     * 选择handler 进行发送
     * @param serviceKey 服务名 & 版本标识
     * @param loadBalance 负载均衡实例
     * @return handler
     * @throws Exception Client close
     */
    public static RpcClientHandler chooseHandler(String serviceKey, RpcLoadBalance loadBalance) throws Exception {
        Map<RpcMetaData, RpcClientHandler> connectedServerNodes = Connector.getInstance().getConnectedServerNodes();
        while (connectedServerNodes.values().size() <= 0) {
            if (!ConnectUpdater.getInstance().isRunning()) {
                throw new RuntimeException("Client is closed.");
            }
            try {
                waitingForHandler();
            } catch (InterruptedException e) {
                logger.error("Waiting for available service is interrupted!", e);
            }
        }
        RpcMetaData rpcMetaData = loadBalance.route(serviceKey);
        RpcClientHandler handler = connectedServerNodes.get(rpcMetaData);
        if (handler == null) {
            throw new Exception("Can not get available connection.");
        }
        return handler;
    }

    /**
     * 唤醒被阻塞的线程
     */
    static void signalAvailableHandler() {
        lock.lock();
        try {
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 等待handler
     */
    private static void waitingForHandler() throws InterruptedException {
        lock.lock();
        try {
            logger.warn("Waiting for available service.");
            condition.await(HANDLER_RETRY_TIME_INTERVAL, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

}
