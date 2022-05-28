package com.polyu.rpc.test.service;

import com.polyu.rpc.annotation.BRpcProvider;

@BRpcProvider(value = HelloService2.class, version = "1.0", coreThreadPoolSize = 10, maxThreadPoolSize = 70)
public class HelloServiceImpl2 implements HelloService2 {
    @Override
    public String hello(String name) {
        try {
            Thread.sleep(3000L);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }
}
