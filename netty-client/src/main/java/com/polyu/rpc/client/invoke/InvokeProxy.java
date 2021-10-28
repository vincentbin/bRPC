package com.polyu.rpc.client.invoke;

import com.polyu.rpc.codec.RpcRequest;
import com.polyu.rpc.route.RpcLoadBalance;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

public class InvokeProxy implements InvocationHandler {

    private String version;
    private RpcLoadBalance loadBalance;
    private long timeoutLength;

    public InvokeProxy(String version, RpcLoadBalance loadBalance, long timeoutLength) {
        this.version = version;
        this.loadBalance = loadBalance;
        this.timeoutLength = timeoutLength;
    }

    /**
     * 动态代理调用
     * @param proxy 代理
     * @param method 方法
     * @param args 参数
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);
        request.setVersion(version);

        Invocation invocation = new Invocation(request, this.loadBalance, this.timeoutLength);
        return invocation.invoke();
    }

}
