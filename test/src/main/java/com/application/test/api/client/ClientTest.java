package com.application.test.api.client;

import com.application.test.service.HelloService;
import com.polyu.rpc.client.RpcClient;
import com.polyu.rpc.registry.zookeeper.ZKDiscovery;
import com.polyu.rpc.route.impl.RpcLoadBalanceRoundRobin;

public class ClientTest {

    public static void main(String[] args) throws Exception {
        new RpcClient(new ZKDiscovery("127.0.0.1:2181", "testYYB"));
        HelloService helloService = RpcClient.getProxyInstance(HelloService.class, "1.0", new RpcLoadBalanceRoundRobin(), 3000L);
        String str = helloService.hello("yyb");
        System.out.println("str = " + str);
    }
}
