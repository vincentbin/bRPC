package com.polyu.rpc.client.interceptor.impl;

import com.polyu.rpc.client.interceptor.Interceptor;
import com.polyu.rpc.client.invoke.Invocation;
import com.polyu.rpc.client.result.future.RpcFuture;

public class CallBackInterceptor implements Interceptor {

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
     * 调用回调
     * @param invocation invoke 实例
     */
    @Override
    public void afterInvoke(Invocation invocation) {
        RpcFuture rpcFuture = invocation.getRpcFuture();
        rpcFuture.invokeCallbacks();
    }

}
