package com.polyu.rpc.client.interceptor;

import com.polyu.rpc.client.invoke.Invocation;

/**
 * 拦截器 Interceptor
 */
public interface Interceptor {

    void beforeInvoke(Invocation invocation);

    Object intercept(Invocation invocation);

    void afterInvoke(Invocation invocation);
}
