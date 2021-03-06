package com.polyu.rpc.route;

import com.polyu.rpc.route.impl.RpcLoadBalanceRoundRobin;

public class DefaultRpcLoadBalanceHolder {

    private static final RpcLoadBalance rpcLoadBalance = new RpcLoadBalanceRoundRobin();

    /**
     * θ·εεδΎ rpcLoadBalance
     * @return RpcLoadBalanceRoundRobin
     */
    public static RpcLoadBalance getInstance() {
        return rpcLoadBalance;
    }
}
