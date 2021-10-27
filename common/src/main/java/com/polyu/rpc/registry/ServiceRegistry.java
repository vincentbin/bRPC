package com.polyu.rpc.registry;

import java.util.Map;

public interface ServiceRegistry {

    /**
     * 服务注册
     * @param host 服务提供者ip
     * @param port 服务提供者端口号
     * @param serviceKey2BeanMap serviceKey -> bean 映射
     */
    void registerService(String host, int port, Map<String, Object> serviceKey2BeanMap);

    /**
     * 注销服务
     */
    void unregisterService();
}
