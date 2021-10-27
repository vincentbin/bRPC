package com.polyu.rpc.server.netty;


import com.polyu.rpc.registry.ServiceRegistry;
import com.polyu.rpc.server.Server;
import com.polyu.rpc.util.ServiceUtil;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
public class NettyServer extends Server {
    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    /**
     * 业务线程池核心线程数
     */
    private int coreThreadPoolSize = 30;
    /**
     * 业务线程池最大线程数
     */
    private int maxThreadPoolSize = 65;

    private Thread thread;
    private String serverAddress;
    private ServiceRegistry serviceRegistry;
    private Map<String, Object> serviceKey2BeanMap = new HashMap<>();

    public NettyServer(String serverAddress, ServiceRegistry serviceRegistry) {
        this.serverAddress = serverAddress;
        this.serviceRegistry = serviceRegistry;
    }

    public NettyServer(String serverAddress, ServiceRegistry serviceRegistry, int coreThreadPoolSize, int maxThreadPoolSize) {
       this.serverAddress = serverAddress;
       this.serviceRegistry = serviceRegistry;
       this.coreThreadPoolSize = coreThreadPoolSize;
       this.maxThreadPoolSize = maxThreadPoolSize;
    }

    /**
     * 异步启动netty服务
     */
    public void start() {
        NettyServerBootstrap nettyServerBootstrap = new NettyServerBootstrap(
                coreThreadPoolSize,
                maxThreadPoolSize,
                NettyServer.class.getSimpleName(),
                serverAddress,
                serviceKey2BeanMap,
                serviceRegistry);
        thread = new Thread(nettyServerBootstrap);
        thread.start();
    }

    /**
     * 关闭server
     */
    public void stop() {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }

    /**
     * 添加服务到serviceMap
     * @param interfaceName 接口名
     * @param version 版本
     * @param serviceBean 服务实现类
     */
    public void addService(String interfaceName, String version, Object serviceBean) {
        logger.info("Adding service, interface: {}, version: {}, bean：{}", interfaceName, version, serviceBean);
        String serviceKey = ServiceUtil.makeServiceKey(interfaceName, version);
        serviceKey2BeanMap.put(serviceKey, serviceBean);
    }

    protected void setCoreThreadPoolSize(int coreThreadPoolSize) {
        if (coreThreadPoolSize <= 0) {
            return;
        }
        this.coreThreadPoolSize = coreThreadPoolSize;
    }

    protected void setMaxThreadPoolSize(int maxThreadPoolSize) {
        if (maxThreadPoolSize <= 0) {
            return;
        }
        this.maxThreadPoolSize = maxThreadPoolSize;
    }
}
