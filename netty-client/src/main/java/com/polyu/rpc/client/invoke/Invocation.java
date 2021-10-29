package com.polyu.rpc.client.invoke;

import com.polyu.rpc.client.connect.HandlerManager;
import com.polyu.rpc.client.interceptor.Interceptor;
import com.polyu.rpc.client.interceptor.impl.CallBackInterceptor;
import com.polyu.rpc.client.interceptor.impl.TimeCostInterceptor;
import com.polyu.rpc.client.netty.handler.RpcClientHandler;
import com.polyu.rpc.client.result.future.RpcFuture;
import com.polyu.rpc.codec.RpcRequest;
import com.polyu.rpc.route.DefaultRpcLoadBalanceHolder;
import com.polyu.rpc.route.RpcLoadBalance;
import com.polyu.rpc.util.ServiceUtil;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Data
public class Invocation {
    private static final Logger logger = LoggerFactory.getLogger(Invocation.class);

    private List<Interceptor> interceptors = new ArrayList<>();
    private int index = 0;
    private RpcRequest rpcRequest;
    private RpcLoadBalance loadBalance;
    private long timeoutLength;
    private RpcFuture rpcFuture;

    public Invocation(RpcRequest rpcRequest, RpcLoadBalance loadBalance, long timeoutLength) {
        this.rpcRequest = rpcRequest;
        this.loadBalance = loadBalance;
        this.timeoutLength = timeoutLength;

        this.interceptors.add(new CallBackInterceptor());
        this.interceptors.add(new TimeCostInterceptor());
    }

    /**
     * invoke with interceptor
     * @return rpc result
     */
    public Object invoke() {
        if (index == interceptors.size()) {
            return this.sendRequest();
        }
        Interceptor interceptor = interceptors.get(index++);
        return interceptor.intercept(this);
    }

    /**
     * 发送请求
     * @return result
     */
    private Object sendRequest() {
        Object res = null;
        try {
            String serviceKey = ServiceUtil.makeServiceKey(rpcRequest.getClassName(), rpcRequest.getVersion());
            RpcClientHandler handler = HandlerManager.chooseHandler(serviceKey, loadBalance == null ? DefaultRpcLoadBalanceHolder.getInstance() : loadBalance);
            RpcFuture rpcFuture = handler.sendRequest(this.rpcRequest, this.timeoutLength);
            this.rpcFuture = rpcFuture;
            res = rpcFuture.get();
        } catch (Exception e) {
            logger.error("Invoke exception, exception: {}.", e.getMessage(), e);
        }
        return res;
    }

}
