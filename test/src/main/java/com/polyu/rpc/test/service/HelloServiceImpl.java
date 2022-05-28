package com.polyu.rpc.test.service;

import com.polyu.rpc.annotation.BRpcProvider;

@BRpcProvider(value = HelloService.class, version = "1.0", coreThreadPoolSize = 10, maxThreadPoolSize = 70)
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String name) {
        return name;
    }

}
