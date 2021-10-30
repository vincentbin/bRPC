package com.polyu.rpc.client.result;

import com.polyu.rpc.client.result.future.RpcFuture;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PendingRpcHolder {

    private static final ConcurrentHashMap<String, RpcFuture> pendingRPC = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private static long timeoutCheckInterval = 1000L;

    /**
     * 启动超时检查线程
     */
    public static void startTimeoutThreadPool() {
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                for (String requestId : pendingRPC.keySet()) {
                    RpcFuture rpcFuture = pendingRPC.get(requestId);
                    if (rpcFuture == null || !rpcFuture.isTimeout()) {
                        continue;
                    }
                    rpcFuture.cancel(true);
                    pendingRPC.remove(requestId);
                }
            }
        }, 3000L, timeoutCheckInterval, TimeUnit.MILLISECONDS);
    }

    /**
     * client 关闭同时调用关闭线程池资源
     */
    public static void stop() {
        scheduledExecutorService.shutdown();
    }

    /**
     * 获取pendingRPC
     * @return map
     */
    public static ConcurrentHashMap<String, RpcFuture> getPendingRPC() {
        return pendingRPC;
    }

    /**
     * 设置巡查时间间隔
     * @param timeoutCheckInterval 时间间隔
     */
    public static void setTimeoutCheckInterval(long timeoutCheckInterval) {
        PendingRpcHolder.timeoutCheckInterval = timeoutCheckInterval;
    }

}
