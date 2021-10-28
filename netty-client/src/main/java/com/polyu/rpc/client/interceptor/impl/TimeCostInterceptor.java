package com.polyu.rpc.client.interceptor.impl;

import com.polyu.rpc.client.interceptor.Interceptor;
import com.polyu.rpc.client.invoke.Invocation;
import com.polyu.rpc.client.result.future.RpcFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeCostInterceptor implements Interceptor {
    private static final Logger logger = LoggerFactory.getLogger(TimeCostInterceptor.class);

    @Override
    public void beforeInvoke(Invocation invocation) {

    }

    @Override
    public Object intercept(Invocation invocation) {
        this.beforeInvoke(invocation);
        Object invokeResult = invocation.invoke();
        this.afterInvoke(invocation);
        return invokeResult;
    }

    /**
     * 超时异常处理
     * @param invocation 调用实例
     */
    @Override
    public void afterInvoke(Invocation invocation) {
        RpcFuture rpcFuture = invocation.getRpcFuture();
        long timeEnd = System.currentTimeMillis();
        long timeStart = rpcFuture.getStartTime();
        if (timeEnd - timeStart > invocation.getTimeoutLength()) {
            logger.error("Invoke Timeout. timeStart: {}, timeEnd: {}.", timeStart, timeEnd);
            rpcFuture.setTimeoutException();
            throw rpcFuture.getTimeoutException();
        }
        rpcFuture.invokeCallbacks();
    }

}
