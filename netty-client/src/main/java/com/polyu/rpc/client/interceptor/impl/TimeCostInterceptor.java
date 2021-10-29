package com.polyu.rpc.client.interceptor.impl;

import com.polyu.rpc.client.interceptor.Interceptor;
import com.polyu.rpc.client.invoke.Invocation;
import com.polyu.rpc.client.result.future.RpcFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 保证此拦截器顺序 如果超时则不执行的逻辑在该拦截器之前加入
 */
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
     * 惰性删除 or 提前释放
     * 都会在此抛 timeout 异常
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
    }

}
