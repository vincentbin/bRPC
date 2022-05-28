package com.polyu.rpc.test.spring.client;

import com.polyu.rpc.test.service.HelloService;
import com.polyu.rpc.test.service.HelloService2;
import com.polyu.rpc.annotation.BRpcConsumer;
import com.polyu.rpc.route.impl.RpcLoadBalanceRandom;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ClientTest {

    @BRpcConsumer(version = "1.0", loadBalanceStrategy = RpcLoadBalanceRandom.class)
    static HelloService2 helloService2;

    @BRpcConsumer(version = "1.0")
    static HelloService helloService;

    /**
     * api
     * @param args
     */
    public static void main(String[] args) throws InterruptedException {
        ClassPathXmlApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext("client-spring.xml");
        String name1 = helloService.hello("yyb");
        System.out.println("name = " + name1);
//        String name2 = helloService2.hello("cyx");
//        System.out.println("name = " + name2);
        classPathXmlApplicationContext.close();
    }
}
