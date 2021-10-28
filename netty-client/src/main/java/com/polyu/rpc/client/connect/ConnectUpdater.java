package com.polyu.rpc.client.connect;

import com.polyu.rpc.registry.observation.Observer;
import com.polyu.rpc.registry.ServiceDiscovery;
import com.polyu.rpc.route.MetaDataKeeper;
import com.polyu.rpc.info.RpcMetaData;
import com.polyu.rpc.client.netty.handler.RpcClientHandler;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

public class ConnectUpdater implements Observer {
    private static final Logger logger = LoggerFactory.getLogger(ConnectUpdater.class);

    private CopyOnWriteArraySet<RpcMetaData> rpcMetaDataSet = new CopyOnWriteArraySet<>();

    private volatile boolean isRunning = true;
    private ServiceDiscovery serviceDiscovery;

    /**
     * 观察者模式 持有事件
     * @param serviceDiscovery 服务发现
     */
    private ConnectUpdater(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    /**
     * zk 根据事件进行更新
     * nacos 没有事件类型更新
     * @param rpcMetaDatas rpc server信息
     * @param type 更新类型（nacos更新类型以及zk全量更新为null）
     */
    @Override
    public void update(List<RpcMetaData> rpcMetaDatas, PathChildrenCacheEvent.Type type) {
        if (type == null) {
            updateConnectedServer(rpcMetaDatas);
            return;
        }
        RpcMetaData rpcMetaData = rpcMetaDatas.get(0);
        updateConnectedServer(rpcMetaData, type);
    }

    /**
     * dcl单例
     */
    private static class SingletonHolder {
        private static volatile ConnectUpdater instance;

        static ConnectUpdater getInstance(ServiceDiscovery discovery) {
            if (instance == null) {
                synchronized (SingletonHolder.class) {
                    if (instance == null) {
                        instance = new ConnectUpdater(discovery);
                        // 注册观察事件
                        instance.getServiceDiscovery().registerObserver(instance);
                    }
                }
            }
            return instance;
        }

    }

    /**
     * 获取并初始化实例
     * @return
     */
    public static ConnectUpdater getAndInitInstance(ServiceDiscovery discovery) {
        return SingletonHolder.getInstance(discovery);
    }

    /**
     * 初始化后获取单例方法
     * @return
     */
    public static ConnectUpdater getInstance() {
        return SingletonHolder.instance;
    }

    /**
     * 全量更新连接
     * @param serviceList
     */
    private void updateConnectedServer(List<RpcMetaData> serviceList) {
        if (serviceList != null && serviceList.size() > 0) {
            HashSet<RpcMetaData> serviceSet = new HashSet<>(serviceList.size());
            serviceSet.addAll(serviceList);

            // 加入 & 连接原来没有的
            for (final RpcMetaData rpcMetaData : serviceSet) {
                if (!rpcMetaDataSet.contains(rpcMetaData)) {
                    Connector.getInstance().connectServerNode(rpcMetaData);
                }
            }

            // 关闭 & 删除现在去处的
            for (RpcMetaData rpcMetaData : rpcMetaDataSet) {
                if (!serviceSet.contains(rpcMetaData)) {
                    logger.info("Remove invalid service: {}.", rpcMetaData.toJson());
                    Connector.getInstance().removeAndCloseHandler(rpcMetaData);
                }
            }
        } else {
            logger.error("No available service!");
            for (RpcMetaData rpcMetaData : rpcMetaDataSet) {
                Connector.getInstance().removeAndCloseHandler(rpcMetaData);
            }
        }
    }

    /**
     * 根据类型增量更新连接
     * @param rpcMetaData 元数据
     * @param type 更新类型（zk特有）
     */
    public void updateConnectedServer(RpcMetaData rpcMetaData, PathChildrenCacheEvent.Type type) {
        if (rpcMetaData == null) {
            return;
        }
        if (type == PathChildrenCacheEvent.Type.CHILD_ADDED && !rpcMetaDataSet.contains(rpcMetaData)) {
            Connector.getInstance().connectServerNode(rpcMetaData);
        } else if (type == PathChildrenCacheEvent.Type.CHILD_UPDATED) {
            // 对于主机ip & port没有改变的zk child更新，不进行重新连接。直接更新connectedServerNodes
            RpcMetaDataChanger rpcMetaDataChanger = serverHostUnChange(rpcMetaData);
            if (rpcMetaDataChanger.isNeedChange()) {
                RpcMetaData oldProtocol = rpcMetaDataChanger.getOldMetaData();
                Map<RpcMetaData, RpcClientHandler> connectedServerNodes = Connector.getInstance().getConnectedServerNodes();
                RpcClientHandler rpcClientHandler = connectedServerNodes.get(oldProtocol);
                connectedServerNodes.put(rpcMetaData, rpcClientHandler);
                connectedServerNodes.remove(oldProtocol);

                rpcMetaDataSet.add(rpcMetaData);
                rpcMetaDataSet.remove(oldProtocol);

                MetaDataKeeper.removeZkChild(oldProtocol);
                MetaDataKeeper.addZkChild(rpcMetaData);
                return;
            }
            Connector.getInstance().removeAndCloseHandler(rpcMetaData);
            Connector.getInstance().connectServerNode(rpcMetaData);
        } else if (type == PathChildrenCacheEvent.Type.CHILD_REMOVED) {
            Connector.getInstance().removeAndCloseHandler(rpcMetaData);
        } else {
            throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    /**
     * 判断zk节点变更时 host ip & port是否改变
     * 返回是否需要改变判断 & 需要替换的protocol
     * @return
     */
    private RpcMetaDataChanger serverHostUnChange(RpcMetaData rpcMetaData) {
        for (RpcMetaData presentProtocol : rpcMetaDataSet) {
            String presentHost = presentProtocol.getHost();
            int presentPort = presentProtocol.getPort();
            if (presentHost != null && !"".equals(presentHost)) {
                if (presentHost.equals(rpcMetaData.getHost()) && presentPort == rpcMetaData.getPort()) {
                    return new RpcMetaDataChanger(true, presentProtocol);
                }
            }
        }
        return new RpcMetaDataChanger(false);
    }

    @Data
    @NoArgsConstructor
    private static class RpcMetaDataChanger {
        boolean needChange;
        RpcMetaData oldMetaData;

        RpcMetaDataChanger(boolean needChange, RpcMetaData oldMetaData) {
            this.needChange = needChange;
            this.oldMetaData = oldMetaData;
        }

        RpcMetaDataChanger(boolean needChange) {
            this.needChange = needChange;
        }
    }

    public CopyOnWriteArraySet<RpcMetaData> getRpcMetaDataSet() {
        return rpcMetaDataSet;
    }

    /**
     * 获取服务发现实例
     * @return serviceDiscoveryImpl
     */
    public ServiceDiscovery getServiceDiscovery() {
        return serviceDiscovery;
    }

    /**
     * 获取运行状态
     * @return boolean
     */
    boolean isRunning() {
        return isRunning;
    }

    public void stop() {
        isRunning = false;
        for (RpcMetaData rpcMetaData : rpcMetaDataSet) {
            Connector.getInstance().removeAndCloseHandler(rpcMetaData);
        }
        HandlerManager.signalAvailableHandler();
        Connector.getInstance().getConnectionThreadPool().shutdown();
        Connector.getInstance().getEventLoopGroup().shutdownGracefully();
    }

}
