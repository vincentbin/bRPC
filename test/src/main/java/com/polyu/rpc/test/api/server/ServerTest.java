package com.polyu.rpc.test.api.server;

import com.polyu.rpc.test.service.HelloService;
import com.polyu.rpc.test.service.HelloService2;
import com.polyu.rpc.test.service.HelloServiceImpl;
import com.polyu.rpc.test.service.HelloServiceImpl2;
import com.polyu.rpc.registry.ServiceRegistry;
import com.polyu.rpc.registry.zookeeper.ZKRegistry;
import com.polyu.rpc.server.netty.NettyServer;

public class ServerTest {

    public static void main(String[] args) throws Exception {
//        String serverAddress = "127.0.0.1:18877";
//        String serverAddress = "127.0.0.1:18876";
//        String serverAddress = "127.0.0.1:18875";
        String serverAddress = "127.0.0.1:18874";

        // zk
        String registryAddress = "127.0.0.1:2181";
        // nacos
//        String registryAddress = "127.0.0.1:8848";
        ServiceRegistry serviceRegistry = new ZKRegistry(registryAddress, "springbootApplication");
        NettyServer rpcServer = new NettyServer(serverAddress, serviceRegistry);
        HelloService helloService1 = new HelloServiceImpl();
        HelloServiceImpl2  helloService2 = new HelloServiceImpl2();
        rpcServer.addService(HelloService.class.getName(), "1.0", helloService1);
        rpcServer.addService(HelloService2.class.getName(), "1.0", helloService2);
        try {
            rpcServer.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
