package com.polyu.rpc.util;

import com.polyu.rpc.info.RpcServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service相关工具类
 */
public class ServiceUtil {
    private static final Logger logger = LoggerFactory.getLogger(ServiceUtil.class);

    private static final String SERVICE_CONCAT_TOKEN = "#";

    /**
     * 服务serviceKey生成
     * @param interfaceName 接口名
     * @param version 版本
     * @return key字符串
     */
    public static String makeServiceKey(String interfaceName, String version) {
        String serviceKey = interfaceName;
        if (version != null && version.trim().length() > 0) {
            serviceKey += SERVICE_CONCAT_TOKEN.concat(version);
        }
        return serviceKey;
    }

    /**
     * 由 serviceKey2BeanMap 生成 RpcServiceInfo List
     * @param serviceKey2BeanMap serviceKey -> 实现类 bean
     * @return 服务信息列表
     */
    public static List<RpcServiceInfo> beanMap2RpcServiceInfos(Map<String, Object> serviceKey2BeanMap) {
        List<RpcServiceInfo> serviceInfoList = new ArrayList<>();
        for (String key : serviceKey2BeanMap.keySet()) {
            String[] serviceInfo = key.split(ServiceUtil.SERVICE_CONCAT_TOKEN);
            if (serviceInfo.length > 0) {
                RpcServiceInfo rpcServiceInfo = new RpcServiceInfo();
                rpcServiceInfo.setServiceName(serviceInfo[0]);
                if (serviceInfo.length == 2) {
                    rpcServiceInfo.setVersion(serviceInfo[1]);
                } else {
                    rpcServiceInfo.setVersion("");
                }
                logger.info("Register new service: {}.", key);
                serviceInfoList.add(rpcServiceInfo);
            } else {
                logger.warn("Can not get service name and version: {}.", key);
            }
        }
        return serviceInfoList;
    }
}
