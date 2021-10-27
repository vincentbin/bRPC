package com.polyu.rpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务端注解
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface BRpcProvider {

    Class<?> value();

    /**
     * 版本号
     */
    String version() default "";

    /**
     * 要求:
     * coreThreadPoolSize > 0
     * coreThreadPoolSize <= maxThreadPoolSize
     * 否则配置无效 走默认设置参数值
     */
    int coreThreadPoolSize() default 30;

    /**
     * 要求:
     * maxThreadPoolSize > 0
     * 否则配置无效 走默认设置参数值
     */
    int maxThreadPoolSize() default 65;
}
