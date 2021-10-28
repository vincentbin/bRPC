package com.polyu.rpc.client.interceptor;

import com.polyu.rpc.client.invoke.Invocation;

/**
 * 拦截器 Interceptor
 */
public interface Interceptor {

    /**
     * 前处理
     * @param invocation invoke 实例
     */
    void beforeInvoke(Invocation invocation);

    /**
     * 拦截调用
     * @param invocation invoke 实例
     * @return request result
     */
    Object intercept(Invocation invocation);

    /**
     * 后处理
     * @param invocation invoke 实例
     */
    void afterInvoke(Invocation invocation);
}
