package com.polyu.rpc.server.netty.handler;

import com.polyu.rpc.codec.HeartBeat;
import com.polyu.rpc.codec.RpcRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeartBeatHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(HeartBeatHandler.class);

    /**
     * 拦截client发送的心跳
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof RpcRequest) {
            RpcRequest request = (RpcRequest) msg;
            if (HeartBeat.BEAT_ID.equalsIgnoreCase(request.getRequestId())) {
                logger.info("Server read heartbeat ping from {}", ctx.channel().remoteAddress().toString());
                return;
            }
        }
        super.channelRead(ctx, msg);
    }

    /**
     * 空闲时间超时则主动关闭连接
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ctx.channel().close();
            logger.warn("Channel idle in last {} seconds, close it", HeartBeat.BEAT_TIMEOUT);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
