package com.polyu.rpc.route.impl;

import com.polyu.rpc.info.RpcMetaData;
import com.polyu.rpc.route.MetaDataKeeper;
import com.polyu.rpc.route.RpcLoadBalance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

/**
 * 随机
 */
public class RpcLoadBalanceRandom implements RpcLoadBalance {

    private Random random;
    private static final Logger logger = LoggerFactory.getLogger(RpcLoadBalanceRandom.class);

    public RpcLoadBalanceRandom() {
        this.random = new Random();
    }

    private RpcMetaData doRoute(List<RpcMetaData> addressList) {
        int index = random.nextInt(addressList.size());
        return addressList.get(index);
    }

    @Override
    public RpcMetaData route(String serviceKey) throws Exception {
        logger.debug("RpcLoadBalanceRandom is routing for {}.", serviceKey);
        List<RpcMetaData> addressList = MetaDataKeeper.getProtocolsFromServiceKey(serviceKey);
        if (addressList != null && addressList.size() > 0) {
            return doRoute(addressList);
        } else {
            throw new Exception("Can not find connection for service: " + serviceKey);
        }
    }
}
