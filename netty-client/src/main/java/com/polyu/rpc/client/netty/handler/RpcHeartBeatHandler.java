package com.polyu.rpc.client.netty.handler;

import com.polyu.rpc.codec.HeartBeat;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

public class RpcHeartBeatHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(RpcHeartBeatHandler.class);

    private SocketAddress remotePeer;
    private volatile Channel channel;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.remotePeer = this.channel.remoteAddress();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    /**
     * 心跳事件进行处理
     *
     * @param ctx context
     * @param evt event
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            switch (event.state()) {
                case WRITER_IDLE:
                    logger.info("heart beat WRITER_IDLE event triggered.");
                    break;
                case READER_IDLE:
                    logger.info("heart beat READER_IDLE event triggered.");
                    break;
                case ALL_IDLE:
                    sendHeartBeatPackage();
                    logger.info("heart beat ALL_IDLE event triggered.");
                    break;
            }
        }
    }

    /**
     * 发送心跳包
     */
    private void sendHeartBeatPackage() {
        logger.info("Client send beat-ping to {}.", remotePeer);
        try {
            channel.writeAndFlush(HeartBeat.BEAT_PING);
        } catch (Exception e) {
            logger.error("Send heartBeatPackage exception: {}.", e.getMessage());
        }
    }

}
