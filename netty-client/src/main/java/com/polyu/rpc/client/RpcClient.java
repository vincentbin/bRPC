package com.polyu.rpc.client;

import com.polyu.rpc.annotation.BRpcConsumer;
import com.polyu.rpc.client.connect.ConnectUpdater;
import com.polyu.rpc.client.invoke.InvokeProxy;
import com.polyu.rpc.client.result.PendingRpcHolder;
import com.polyu.rpc.registry.ServiceDiscovery;
import com.polyu.rpc.route.RpcLoadBalance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RpcClient implements ApplicationContextAware, DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    private ServiceDiscovery serviceDiscovery;
    /**
     * 异步请求/callback 线程池
     */
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16,
            600L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000));

    /**
     * 注册中心地址 & 注册中心选型
     * @param serviceDiscovery 注册中心选型 nacos / zk
     */
    public RpcClient(ServiceDiscovery serviceDiscovery) {
        ConnectUpdater connectUpdater = ConnectUpdater.getAndInitInstance(serviceDiscovery);
        this.serviceDiscovery = connectUpdater.getServiceDiscovery();
        this.serviceDiscovery.discoveryService();
        PendingRpcHolder.startTimeoutThreadPool();
    }

    @SuppressWarnings("unchecked")
    public static <T, P> T getProxyInstance(Class<T> interfaceClass, String version, RpcLoadBalance loadBalance, long timeoutLength) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new InvokeProxy(version, loadBalance, timeoutLength)
        );
    }

    public static void submit(Runnable task) {
        threadPoolExecutor.submit(task);
    }

    private void stop() {
        threadPoolExecutor.shutdown();
        serviceDiscovery.stop();
        ConnectUpdater.getInstance().stop();
        PendingRpcHolder.stop();
    }

    @Override
    public void destroy() {
        this.stop();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Field[] fields = bean.getClass().getDeclaredFields();
            try {
                for (Field field : fields) {
                    BRpcConsumer rpcAutowired = field.getAnnotation(BRpcConsumer.class);
                    if (rpcAutowired != null) {
                        String version = rpcAutowired.version();
                        RpcLoadBalance loadBalance = (RpcLoadBalance) rpcAutowired.loadBalanceStrategy().newInstance();
                        long timeoutLength = rpcAutowired.timeOutLength();
                        field.setAccessible(true);
                        field.set(bean, getProxyInstance(field.getType(), version, loadBalance, timeoutLength));
                    }
                }
            } catch (Exception e) {
                logger.error(e.toString());
            }
        }
    }
}

