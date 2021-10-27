package com.polyu.rpc.server.netty.handler;

import com.polyu.rpc.codec.RpcRequest;
import com.polyu.rpc.server.task.BusinessTask;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * netty server business logic process handler
 */
public class BusinessHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(BusinessHandler.class);

    private final Map<String, Object> serviceKey2BeanMap;

    /**
     * 业务线程池
     */
    private final ThreadPoolExecutor businessTaskThreadPool;

    public BusinessHandler(Map<String, Object> serviceKey2BeanMap, final ThreadPoolExecutor businessTaskThreadPool) {
        this.serviceKey2BeanMap = serviceKey2BeanMap;
        this.businessTaskThreadPool = businessTaskThreadPool;
    }

    /**
     * 获取rpcRequest进行业务处理
     * @param ctx
     * @param msg 信息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        businessTaskThreadPool.execute(new BusinessTask((RpcRequest) msg, serviceKey2BeanMap, ctx));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.warn("Server caught exception: " + cause.getMessage());
        ctx.close();
    }

}
