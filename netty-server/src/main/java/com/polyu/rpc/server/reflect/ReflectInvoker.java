package com.polyu.rpc.server.reflect;

import com.polyu.rpc.codec.RpcRequest;

import java.lang.reflect.Method;

public class ReflectInvoker {

    /**
     * jdk反射调用
     * @param request 请求
     * @param serviceBean 实现类
     * @return
     * @throws Throwable
     */
    public static Object handle(RpcRequest request, Object serviceBean) throws Throwable {
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(serviceBean, parameters);
    }
}
