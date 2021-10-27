package com.polyu.rpc.registry;

/**
 * 注册中心配置枚举
 */
public enum RegistryConfigEnum {

    ZK_REGISTRY_PATH("/registry/"),
    ZK_NAME_SPACE("bRPC"),
    ZK_SESSION_TIMEOUT(5000),
    ZK_CONNECTION_TIMEOUT(5000),

    NACOS_REGISTRY_PATH("bRPC.");


    private String value;
    private int timeOutLength;

    RegistryConfigEnum(String value) {
        this.value = value;
    }

    RegistryConfigEnum(int timeOutLength) {
        this.timeOutLength = timeOutLength;
    }

    public String getValue() {
        return value;
    }

    public int getTimeOutLength() {
        return timeOutLength;
    }

}
