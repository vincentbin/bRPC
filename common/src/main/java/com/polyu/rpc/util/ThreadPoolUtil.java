package com.polyu.rpc.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolUtil {
    private static final long KEEP_ALIVE_TIME = 60L;
    private static final int BLOCKING_QUEUE_CAPACITY = 1000;

    /**
     * 构造server线程池
     * @param serviceName 服务接口名
     * @param corePoolSize 核心线程数
     * @param maxPoolSize 最大线程数
     * @return
     */
    public static ThreadPoolExecutor makeServerThreadPool(final String serviceName, int corePoolSize, int maxPoolSize) {
        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(BLOCKING_QUEUE_CAPACITY),
                r -> new Thread(r, getThreadName(serviceName, r)),
                new ThreadPoolExecutor.AbortPolicy());
    }

    public static ThreadPoolExecutor makeThreadPool(int corePoolSize, int maxPoolSize, long keepAliveTime) {
        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(BLOCKING_QUEUE_CAPACITY),
                Thread::new,
                new ThreadPoolExecutor.AbortPolicy());
    }

    private static String getThreadName(final String serviceName, Runnable runnable) {
        return "netty-rpc-" +
                serviceName +
                "-" +
                runnable.hashCode();
    }
}
