package com.polyu.rpc.route.impl;

import com.google.common.hash.Hashing;
import com.polyu.rpc.info.RpcMetaData;
import com.polyu.rpc.route.MetaDataKeeper;
import com.polyu.rpc.route.RpcLoadBalance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 一致性哈希
 */
public class RpcLoadBalanceConsistentHash implements RpcLoadBalance {
    private static final Logger logger = LoggerFactory.getLogger(RpcLoadBalanceConsistentHash.class);

    private RpcMetaData doRoute(String serviceKey, List<RpcMetaData> addressList) {
        int index = Hashing.consistentHash(serviceKey.hashCode(), addressList.size());
        return addressList.get(index);
    }

    @Override
    public RpcMetaData route(String serviceKey) throws Exception {
        logger.debug("RpcLoadBalanceConsistentHash is routing for {}.", serviceKey);
        List<RpcMetaData> addressList = MetaDataKeeper.getProtocolsFromServiceKey(serviceKey);
        if (addressList == null || addressList.isEmpty()) {
            throw new Exception("Can not find connection for service: " + serviceKey);
        }
        return doRoute(serviceKey, addressList);
    }
}
