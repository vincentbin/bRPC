package com.polyu.rpc.annotation;

import com.polyu.rpc.route.impl.RpcLoadBalanceRoundRobin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 客户端注解
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BRpcConsumer {

    /**
     * 版本号
     */
    String version() default "";

    /**
     * 负载均衡策略设置
     * 可选：
     *      RpcLoadBalanceRoundRobin.class(default) / RpcLoadBalanceRandom.class / RpcLoadBalanceConsistentHash.class
     */
    Class<?> loadBalanceStrategy() default RpcLoadBalanceRoundRobin.class;

    /**
     * 接口超时时间
     */
    long timeOutLength() default 3000L;
}