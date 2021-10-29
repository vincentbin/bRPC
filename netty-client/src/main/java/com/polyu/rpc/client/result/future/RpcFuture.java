package com.polyu.rpc.client.result.future;

import com.polyu.rpc.client.RpcClient;
import com.polyu.rpc.codec.RpcRequest;
import com.polyu.rpc.codec.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.*;


public class RpcFuture implements Future<Object> {
    private static final Logger logger = LoggerFactory.getLogger(RpcFuture.class);

    private Semaphore semaphore;
    private RpcRequest request;
    private RpcResponse response;
    private long startTime;
    private long responseTimeThreshold;
    private List<AsyncRPCCallback> pendingCallbacks = new CopyOnWriteArrayList<>();
    private volatile CancellationException timeoutException;

    public RpcFuture(RpcRequest request, long responseTimeThreshold) {
        this.semaphore = new Semaphore(0);
        this.request = request;
        this.startTime = System.currentTimeMillis();
        this.responseTimeThreshold = responseTimeThreshold;
    }

    @Override
    public boolean isDone() {
        return this.response != null || this.timeoutException != null;
    }

    @Override
    public Object get() throws InterruptedException {
        semaphore.acquire(1);
        if (this.response == null) {
            return null;
        }
        return this.response.getResult();
    }

    @Override
    public Object get(long timeout, @Nonnull TimeUnit unit) throws InterruptedException {
        boolean success = semaphore.tryAcquire(1, timeout, unit);
        if (success) {
            if (this.response == null) {
                return null;
            }
            return this.response.getResult();
        } else {
            throw new RuntimeException("Timeout exception. Request id: " + this.request.getRequestId()
                    + ". Request class name: " + this.request.getClassName()
                    + ". Request method: " + this.request.getMethodName());
        }
    }

    /**
     * 超时异常设置
     */
    public void setTimeoutException() {
        if (this.timeoutException == null) {
            this.timeoutException = new CancellationException("Response timeout for request: " + this.request.getRequestId());
        }
    }

    /**
     * 是否超时
     * @return boolean
     */
    public boolean isTimeout() {
        return System.currentTimeMillis() - this.startTime > responseTimeThreshold;
    }

    @Override
    public boolean isCancelled() {
        return this.timeoutException != null;
    }

    /**
     * 超时取消 释放线程
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        semaphore.release(1);
        return true;
    }

    /**
     * 完成 设置结果
     */
    public void done(RpcResponse response) {
        this.response = response;
        semaphore.release(1);
    }

    /**
     * 为保证性能 this.response 无volatile
     * 需要确保 addCallback 后调用请求发送
     * @param callback 回调
     * @return this
     */
    public RpcFuture addCallback(AsyncRPCCallback callback) {
        try {
            if (isDone()) {
                runCallback(callback);
            } else {
                this.pendingCallbacks.add(callback);
            }
        } catch (Exception e) {
            logger.error("addCallback failed. exception: {}.", e.getMessage(), e);
        }
        return this;
    }

    public void invokeCallbacks() {
        try {
            for (final AsyncRPCCallback callback : pendingCallbacks) {
                runCallback(callback);
            }
        } catch (Exception e) {
            logger.error("invokeCallbacks failed. exception: {}.", e.getMessage(), e);
        }
    }

    private void runCallback(final AsyncRPCCallback callback) {
        final RpcResponse res = this.response;
        RpcClient.submit(new Runnable() {
            @Override
            public void run() {
                if (!res.isError()) {
                    callback.success(res.getResult());
                } else {
                    callback.fail(new RuntimeException("Response error.", new Throwable(res.getError())));
                }
            }
        });
    }

    /**
     * 获取启动时间
     * @return
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * 获取超时异常
     * @return
     */
    public CancellationException getTimeoutException() {
        return timeoutException;
    }

    @Override
    public String toString() {
        return "RpcFuture{" +
                "request=" + request +
                ", response=" + response +
                ", startTime=" + startTime +
                ", responseTimeThreshold=" + responseTimeThreshold +
                ", pendingCallbacks=" + pendingCallbacks +
                ", timeoutException=" + timeoutException +
                '}';
    }
}
