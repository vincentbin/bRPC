package com.polyu.rpc.client.spring;

import com.polyu.rpc.client.RpcClient;
import com.polyu.rpc.client.result.PendingRpcHolder;
import com.polyu.rpc.registry.ServiceDiscovery;
import com.polyu.rpc.registry.nacos.NacosDiscovery;
import com.polyu.rpc.registry.zookeeper.ZKDiscovery;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;

public class ClientAutoConfig {

    private static final String NACOS_CONFIG_TYPE = "nacos";
    private static final String ZK_CONFIG_TYPE = "zookeeper";

    /**
     * 订阅目标服务应用名
     */
    @Value("${bRPC.client.registry.target.name}")
    private String targetApplicationName;

    @Value("${bRPC.client.registry.type}")
    private String registryCenter;

    @Value("${bRPC.client.registry.address}")
    private String registryAddress;

    @Value("${bRPC.client.timeout.checkInterval:#{1500L}}")
    private Long timeoutCheckInterval;

    @Bean
    public RpcClient createRpcClientBean() throws Exception {
        ServiceDiscovery serviceDiscovery = null;
        PendingRpcHolder.setTimeoutCheckInterval(this.timeoutCheckInterval);
        if (registryCenter != null && !"".equals(registryAddress)) {
            switch (registryCenter) {
                case NACOS_CONFIG_TYPE:
                    serviceDiscovery = new NacosDiscovery(registryAddress, targetApplicationName);
                    break;
                case ZK_CONFIG_TYPE:
                    serviceDiscovery = new ZKDiscovery(registryAddress, targetApplicationName);
                    break;
                default:
                    throw new Exception("Wrong type of registry type for " + registryCenter);
            }
        }
        return new RpcClient(serviceDiscovery);
    }

}
