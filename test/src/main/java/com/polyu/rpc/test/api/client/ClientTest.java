package com.polyu.rpc.test.api.client;

import com.polyu.rpc.test.service.HelloService;
import com.polyu.rpc.client.RpcClient;
import com.polyu.rpc.registry.zookeeper.ZKDiscovery;
import com.polyu.rpc.route.impl.RpcLoadBalanceRoundRobin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientTest {

    private static final Logger logger = LoggerFactory.getLogger(ClientTest.class);

    public static void main(String[] args) throws Exception {
        new RpcClient(new ZKDiscovery("127.0.0.1:2181", "testYYB"));
        HelloService helloService = RpcClient.getProxyInstance(HelloService.class, "1.0", new RpcLoadBalanceRoundRobin(), 3000L);
        String str = helloService.hello("yyb");
        logger.info("str = {}", str);
        logger.error("str = {}", str);
    }
}
