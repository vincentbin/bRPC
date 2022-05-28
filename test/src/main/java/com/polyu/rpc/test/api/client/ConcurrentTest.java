package com.polyu.rpc.test.api.client;

import com.polyu.rpc.test.service.HelloService;
import com.polyu.rpc.client.RpcClient;
import com.polyu.rpc.registry.zookeeper.ZKDiscovery;
import com.polyu.rpc.route.impl.RpcLoadBalanceRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;

public class ConcurrentTest {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrentTest.class);

    private static HelloService helloService;

    private static Semaphore semaphore;

    public static void main(String[] args) throws Exception {
        new RpcClient(new ZKDiscovery("127.0.0.1:2181", "testYYB"));
        helloService = RpcClient.getProxyInstance(HelloService.class, "1.0", new RpcLoadBalanceRandom(), 3000L);
        for (int i = 0; i < 50; i++) {
            String res = helloService.hello("Yan Yibin");
            logger.info(res);
        }

        for (int i = 1; i <= 20; i++) {
            Thread[] ts = new Thread[i];
            for (int j = 1; j <= i; j++) {
                ts[j - 1] = new Thread(new Task());
            }
            semaphore = new Semaphore(0);
            long s = System.currentTimeMillis();
            for (int j = 1; j <= i; j++) {
                ts[j - 1].start();
            }
            semaphore.acquire(i);
            long e = System.currentTimeMillis();
            System.out.println("qps = " + i * 7000 / ((e - s) / 1000));
        }
    }

    static class Task implements Runnable {

        @Override
        public void run() {
            for (int i = 0; i < 7000; i++) {
                String res = helloService.hello("Yan Yibin");
                logger.info(res);
            }
            semaphore.release(1);
        }
    }
}
