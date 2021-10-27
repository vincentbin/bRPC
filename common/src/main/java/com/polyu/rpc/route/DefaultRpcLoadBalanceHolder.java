package com.polyu.rpc.route;

import com.polyu.rpc.route.impl.RpcLoadBalanceRoundRobin;

public class DefaultRpcLoadBalanceHolder {

    private static final RpcLoadBalance rpcLoadBalance = new RpcLoadBalanceRoundRobin();

    /**
     * 获取单例 rpcLoadBalance
     * @return RpcLoadBalanceRoundRobin
     */
    public static RpcLoadBalance getInstance() {
        return rpcLoadBalance;
    }
}
