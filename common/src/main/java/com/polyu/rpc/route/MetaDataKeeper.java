package com.polyu.rpc.route;

import com.polyu.rpc.info.RpcMetaData;
import com.polyu.rpc.info.RpcServiceInfo;
import com.polyu.rpc.util.ServiceUtil;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 用于route的快速选择
 */
public class MetaDataKeeper {
    private static final Logger logger = LoggerFactory.getLogger(MetaDataKeeper.class);

    private static Map<String, RpcMetaDataContainer> key2MetaDatas = new ConcurrentHashMap<>();

    @Data
    private static class RpcMetaDataContainer {
        private List<RpcMetaData> rpcMetaData = new CopyOnWriteArrayList<>();
        private Map<RpcMetaData, Integer> metaData2Index = new HashMap<>();
    }

    /**
     * zk发生加入新的RpcProtocol 时更新key2Protocols
     * @param rpcMetaData 注册信息
     */
    public synchronized static void addZkChild(RpcMetaData rpcMetaData) {
        if (Objects.isNull(rpcMetaData)) {
            return;
        }
        List<RpcServiceInfo> serviceInfos = rpcMetaData.getServiceInfoList();
        for (RpcServiceInfo serviceInfo : serviceInfos) {
            try {
                String serviceKey = ServiceUtil.makeServiceKey(serviceInfo.getServiceName(), serviceInfo.getVersion());
                RpcMetaDataContainer rpcMetaDataContainer = key2MetaDatas.get(serviceKey);
                if (Objects.isNull(rpcMetaDataContainer)) {
                    rpcMetaDataContainer = new RpcMetaDataContainer();
                    key2MetaDatas.put(serviceKey, rpcMetaDataContainer);
                }
                List<RpcMetaData> rpcMetaDatas = rpcMetaDataContainer.getRpcMetaData();
                Map<RpcMetaData, Integer> protocol2Index = rpcMetaDataContainer.getMetaData2Index();

                Integer index = protocol2Index.get(rpcMetaData);
                // 如果已经存在 移除进行更新
                if (Objects.nonNull(index)) {
                    rpcMetaDatas.remove(index.intValue());
                }
                protocol2Index.put(rpcMetaData, rpcMetaDatas.size());
                rpcMetaDatas.add(rpcMetaData);
            } catch (Exception e) {
                logger.error("addZkChild operation exception, serviceInfo: {}, exception: {}", serviceInfo, e.getMessage());
            }
        }
    }

    /**
     * 删除rpcProtocol 更新key2Protocols
     * @param rpcMetaData
     */
    public synchronized static void removeZkChild(RpcMetaData rpcMetaData) {
        if (Objects.isNull(rpcMetaData)) {
            return;
        }
        List<RpcServiceInfo> serviceInfos = rpcMetaData.getServiceInfoList();
        for (RpcServiceInfo serviceInfo : serviceInfos) {
            try {
                String serviceKey = ServiceUtil.makeServiceKey(serviceInfo.getServiceName(), serviceInfo.getVersion());
                RpcMetaDataContainer rpcMetaDataContainer = key2MetaDatas.get(serviceKey);
                if (Objects.isNull(rpcMetaDataContainer)) {
                    continue;
                }
                Map<RpcMetaData, Integer> protocol2Index = rpcMetaDataContainer.getMetaData2Index();
                List<RpcMetaData> rpcMetaDatas = rpcMetaDataContainer.getRpcMetaData();

                Integer index = protocol2Index.get(rpcMetaData);
                if (Objects.isNull(index)) {
                    continue;
                }
                rpcMetaDatas.remove(index.intValue());
                protocol2Index.remove(rpcMetaData);
            } catch (Exception e) {
                logger.error("removeZkChild operation exception, serviceInfo: {}, exception: {}", serviceInfo, e.getMessage());
            }
        }
    }

    public static List<RpcMetaData> getProtocolsFromServiceKey(String serviceKey) {
        RpcMetaDataContainer rpcMetaDataContainer = key2MetaDatas.get(serviceKey);
        if (Objects.isNull(rpcMetaDataContainer)) {
            logger.warn("there is no service for serviceKey: {}.", serviceKey);
            return null;
        }
        return rpcMetaDataContainer.getRpcMetaData();
    }
}
