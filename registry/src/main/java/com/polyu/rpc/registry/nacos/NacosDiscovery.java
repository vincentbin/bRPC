package com.polyu.rpc.registry.nacos;

import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.polyu.rpc.registry.RegistryConfigEnum;
import com.polyu.rpc.registry.observation.Observer;
import com.polyu.rpc.info.RpcMetaData;
import com.polyu.rpc.registry.ServiceDiscovery;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NacosDiscovery implements ServiceDiscovery {
    private static final Logger logger = LoggerFactory.getLogger(NacosDiscovery.class);

    private NamingService namingService;

    /**
     * 订阅目标server服务名
     */
    private String targetApplicationName;

    /**
     * 观察者引用
     */
    private List<Observer> observers = new ArrayList<>();

    public NacosDiscovery(String registryAddress, String applicationName) {
        try {
            this.namingService = NamingFactory.createNamingService(registryAddress);
        } catch (Exception e) {
            logger.error("Nacos namingService creation failed. exception: {}", e.getMessage());
        }
        this.targetApplicationName = applicationName;
    }

    public NacosDiscovery(String registryAddress) {
        try {
            this.namingService = NamingFactory.createNamingService(registryAddress);
        } catch (Exception e) {
            logger.error("Nacos namingService creation failed. exception: {}", e.getMessage());
        }
        this.targetApplicationName = "DefaultApplication";
    }

    /**
     * 服务发现
     * 首次拉取订阅信息 然后对变更进行监听
     */
    @Override
    public void discoveryService() {
        try {
            String serviceName = RegistryConfigEnum.NACOS_REGISTRY_PATH.getValue().concat(this.targetApplicationName);
            List<Instance> instances = namingService.selectInstances(serviceName, true);
            List<RpcMetaData> rpcMetaData = instances2RpcProtocols(instances);
            // 观察者模式 进行通知
            notifyObserver(rpcMetaData, null);
            logger.info("Nacos service discovery first pull. data: {}.", rpcMetaData);

            namingService.subscribe(serviceName, new EventListener() {
                @Override
                public void onEvent(Event event) {
                    if (event instanceof NamingEvent) {
                        try {
                            NamingEvent namingEvent = (NamingEvent) event;
                            List<Instance> instances = namingEvent.getInstances();
                            List<RpcMetaData> rpcMetaData = instances2RpcProtocols(instances);
                            logger.info("service changed, new server info: {}.", rpcMetaData);
                            // 观察者模式 进行通知
                            notifyObserver(rpcMetaData, null);
                        } catch (Exception e) {
                            logger.error("service update failed. exception: {}.", e.getMessage());
                        }
                    }
                }
            });
        } catch (Exception e) {
            logger.error("discoveryService failed, exception: {}.", e.getMessage());
        }
    }

    /**
     * 终止服务订阅
     */
    @Override
    public void stop() {
        try {
            namingService.shutDown();
        } catch (Exception e) {
            logger.error("NacosDiscovery shutDown failed. exception: {}", e.getMessage());
        }
    }

    /**
     * 转换 nacos instance列表 为 RpcProtocol 列表
     * @param instances nacos 服务实例
     * @return
     */
    private List<RpcMetaData> instances2RpcProtocols(List<Instance> instances) {
        List<RpcMetaData> res = new ArrayList<>(instances.size());
        for (Instance instance : instances) {
            if (!instance.isHealthy()) {
                continue;
            }
            Map<String, String> metadata = instance.getMetadata();
            String rpcProtocolJson = metadata.get("rpcProtocol");
            RpcMetaData rpcMetaData = RpcMetaData.fromJson(rpcProtocolJson);
            res.add(rpcMetaData);
        }
        return res;
    }

    @Override
    public void registerObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void notifyObserver(List<RpcMetaData> protocols, PathChildrenCacheEvent.Type type) {
        for (Observer observer : observers) {
            observer.update(protocols, type);
        }
    }
}
