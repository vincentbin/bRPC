package com.polyu.rpc.route;

import com.polyu.rpc.info.RpcMetaData;

public interface RpcLoadBalance {

    /**
     * 以serviceKey 做负载均衡
     * @param serviceKey serviceName & version
     * @return RpcProtocol
     */
    RpcMetaData route(String serviceKey) throws Exception;

}
