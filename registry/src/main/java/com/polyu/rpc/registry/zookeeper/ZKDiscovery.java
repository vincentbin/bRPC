package com.polyu.rpc.registry.zookeeper;

import com.polyu.rpc.registry.RegistryConfigEnum;
import com.polyu.rpc.registry.observation.Observer;
import com.polyu.rpc.info.RpcMetaData;
import com.polyu.rpc.registry.ServiceDiscovery;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ZKDiscovery implements ServiceDiscovery {
    private static final Logger logger = LoggerFactory.getLogger(ZKDiscovery.class);

    private CuratorClient curatorClient;

    /**
     * 订阅目标server服务名
     */
    private String targetApplicationName;

    /**
     * 观察者引用
     */
    private List<Observer> observers = new ArrayList<>();

    public ZKDiscovery(String registryAddress, String targetApplicationName) {
        this.curatorClient = new CuratorClient(registryAddress);
        this.targetApplicationName = targetApplicationName;
    }

    public ZKDiscovery(String registryAddress) {
        this.curatorClient = new CuratorClient(registryAddress);
        this.targetApplicationName = "DefaultApplication";
    }

    @Override
    public void discoveryService() {
        try {
            logger.info("Get initial service info.");
            getServiceAndUpdateServer();
            curatorClient.watchPathChildrenNode(RegistryConfigEnum.ZK_REGISTRY_PATH.getValue().concat(this.targetApplicationName),
                    new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) {
                    PathChildrenCacheEvent.Type type = pathChildrenCacheEvent.getType();
                    ChildData childData = pathChildrenCacheEvent.getData();
                    switch (type) {
                        case CONNECTION_RECONNECTED:
                            logger.info("Reconnected to zk, try to get latest service list.");
                            getServiceAndUpdateServer();
                            break;
                        case CHILD_ADDED:
                            getServiceAndUpdateServer(childData, PathChildrenCacheEvent.Type.CHILD_ADDED);
                            break;
                        case CHILD_UPDATED:
                            getServiceAndUpdateServer(childData, PathChildrenCacheEvent.Type.CHILD_UPDATED);
                            break;
                        case CHILD_REMOVED:
                            getServiceAndUpdateServer(childData, PathChildrenCacheEvent.Type.CHILD_REMOVED);
                            break;
                    }
                }
            });
        } catch (Exception ex) {
            logger.error("Watch node exception: " + ex.getMessage());
        }
    }

    private void getServiceAndUpdateServer() {
        try {
            List<String> nodeList = curatorClient.getChildren(RegistryConfigEnum.ZK_REGISTRY_PATH.getValue().concat(this.targetApplicationName));
            List<RpcMetaData> dataList = new ArrayList<>();
            for (String node : nodeList) {
                logger.debug("Service node: {}.", node);
                byte[] bytes = curatorClient.getData(RegistryConfigEnum.ZK_REGISTRY_PATH.getValue().concat(this.targetApplicationName) + "/" + node);
                String json = new String(bytes);
                RpcMetaData rpcMetaData = RpcMetaData.fromJson(json);
                dataList.add(rpcMetaData);
            }
            logger.debug("Service node data: {}.", dataList);
            updateConnectedServer(dataList);
        } catch (Exception e) {
            logger.error("Get node exception: {}.", e.getMessage());
        }
    }

    private void getServiceAndUpdateServer(ChildData childData, PathChildrenCacheEvent.Type type) {
        String path = childData.getPath();
        String data = new String(childData.getData(), StandardCharsets.UTF_8);
        logger.info("Child data is updated, path:{}, type:{}, data:{}.", path, type, data);
        RpcMetaData rpcMetaData =  RpcMetaData.fromJson(data);
        updateConnectedServer(rpcMetaData, type);
    }

    private void updateConnectedServer(List<RpcMetaData> dataList) {
        notifyObserver(dataList, null);
    }


    private void updateConnectedServer(RpcMetaData rpcMetaData, PathChildrenCacheEvent.Type type) {
        notifyObserver(Collections.singletonList(rpcMetaData), type);
    }

    @Override
    public void stop() {
        this.curatorClient.close();
    }

    /**
     * 注册观察者
     * @param observer
     */
    @Override
    public void registerObserver(Observer observer) {
        observers.add(observer);
    }

    /**
     * 事件通知
     * @param rpcMetaData
     */
    @Override
    public void notifyObserver(List<RpcMetaData> rpcMetaData, PathChildrenCacheEvent.Type type) {
        for (Observer observer : observers) {
            observer.update(rpcMetaData, type);
        }
    }
}
