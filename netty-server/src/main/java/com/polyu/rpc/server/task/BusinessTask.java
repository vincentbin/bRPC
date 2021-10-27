package com.polyu.rpc.server.task;

import com.polyu.rpc.codec.RpcRequest;
import com.polyu.rpc.codec.RpcResponse;
import com.polyu.rpc.server.reflect.ReflectInvoker;
import com.polyu.rpc.util.ServiceUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class BusinessTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(BusinessTask.class);

    private RpcRequest request;
    private final Map<String, Object> serviceKey2BeanMap;
    private final ChannelHandlerContext ctx;

    public BusinessTask(RpcRequest request, Map<String, Object> serviceKey2BeanMap, ChannelHandlerContext ctx) {
        this.request = request;
        this.serviceKey2BeanMap = serviceKey2BeanMap;
        this.ctx = ctx;
    }

    @Override
    public void run() {
        task();
    }

    /**
     * 写出任务
     */
    private void task() {
        logger.info("Receive request {}.", request.getRequestId());
        RpcResponse response = new RpcResponse();
        response.setRequestId(request.getRequestId());
        try {
            Object result = handle(request);
            response.setResult(result);
        } catch (Throwable t) {
            response.setError(t.toString());
            logger.error("RPC Server handle request error.", t);
        }
        ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                logger.info("Send response for request {}.", request.getRequestId());
            }
        });
    }

    /**
     * 根据service标识获取bean进行调用
     * @param request
     * @return
     * @throws Throwable
     */
    private Object handle(RpcRequest request) throws Throwable {
        String className = request.getClassName();
        String version = request.getVersion();
        String serviceKey = ServiceUtil.makeServiceKey(className, version);
        Object serviceBean = serviceKey2BeanMap.get(serviceKey);
        if (serviceBean == null) {
            logger.error("Can not find service implement with interface name: {} and version: {}.", className, version);
            return null;
        }
        return ReflectInvoker.handle(request, serviceBean);
    }
}
