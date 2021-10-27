package com.polyu.rpc.registry.zookeeper;

import com.polyu.rpc.info.RpcMetaData;
import com.polyu.rpc.info.RpcServiceInfo;
import com.polyu.rpc.registry.RegistryConfigEnum;
import com.polyu.rpc.registry.ServiceRegistry;
import com.polyu.rpc.util.ServiceUtil;
import org.apache.curator.framework.state.ConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class ZKRegistry implements ServiceRegistry {
    private static final Logger logger = LoggerFactory.getLogger(ZKRegistry.class);
    private static final int TIME_OUT_LENGTH = 5000;

    /**
     * zk客户端
     */
    private CuratorClient zkClient;
    private String zkPath;

    /**
     * 应用名
     */
    private String applicationName;

    public ZKRegistry(String registryAddress) {
        this.zkClient = new CuratorClient(registryAddress, TIME_OUT_LENGTH);
        this.applicationName = "DefaultApplication";
    }

    public ZKRegistry(String registryAddress, String applicationName) {
        this.zkClient = new CuratorClient(registryAddress, TIME_OUT_LENGTH);
        this.applicationName = applicationName;
    }

    /**
     * 服务注册
     * @param host 主机地址
     * @param port 端口
     * @param serviceKey2BeanMap 提供服务信息
     */
    @Override
    public void registerService(String host, int port, Map<String, Object> serviceKey2BeanMap) {
        List<RpcServiceInfo> serviceInfoList = ServiceUtil.beanMap2RpcServiceInfos(serviceKey2BeanMap);
        try {
            RpcMetaData rpcMetaData = new RpcMetaData();
            rpcMetaData.setHost(host);
            rpcMetaData.setPort(port);
            rpcMetaData.setServiceInfoList(serviceInfoList);
            String serviceData = rpcMetaData.toJson();
            byte[] bytes = serviceData.getBytes();
            String path = RegistryConfigEnum.ZK_REGISTRY_PATH.getValue().concat(this.applicationName) + "/data-" + rpcMetaData.hashCode();
            path = this.zkClient.createPathData(path, bytes);
            this.zkPath = path;
            logger.info("Register {} new service, host: {}, port: {}.", serviceInfoList.size(), host, port);
        } catch (Exception e) {
            logger.error("Register service fail, exception: {}.", e.getMessage());
        }

        zkClient.addConnectionStateListener((curatorFramework, connectionState) -> {
            if (connectionState == ConnectionState.RECONNECTED) {
                logger.info("Connection state: {}, register service after reconnected.", connectionState);
                registerService(host, port, serviceKey2BeanMap);
            }
        });
    }

    /**
     * 注销服务
     */
    @Override
    public void unregisterService() {
        logger.info("Unregister service.");
        try {
            this.zkClient.deletePath(zkPath);
        } catch (Exception ex) {
            logger.error("Delete service path error: {}.", ex.getMessage());
        }
        this.zkClient.close();
    }
}
