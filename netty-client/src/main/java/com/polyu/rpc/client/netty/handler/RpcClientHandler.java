package com.polyu.rpc.client.netty.handler;

import com.polyu.rpc.client.connect.Connector;
import com.polyu.rpc.client.result.PendingRpcHolder;
import com.polyu.rpc.codec.RpcRequest;
import com.polyu.rpc.codec.RpcResponse;
import com.polyu.rpc.info.RpcMetaData;
import com.polyu.rpc.client.result.future.RpcFuture;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private static final Logger logger = LoggerFactory.getLogger(RpcClientHandler.class);

    private volatile Channel channel;
    private RpcMetaData rpcMetaData;

    private volatile boolean intentionalClose;

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, RpcResponse response) {
        String requestId = response.getRequestId();
        logger.debug("Receive response: {}.", requestId);
        RpcFuture rpcFuture = PendingRpcHolder.getPendingRPC().get(requestId);
        if (rpcFuture == null) {
            return;
        }
        PendingRpcHolder.getPendingRPC().remove(requestId);
        rpcFuture.done(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Client caught exception: {}.", cause.getMessage());
        ctx.close();
    }

    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 发送请求
     * @param request RpcRequest
     * @return result future
     */
    public RpcFuture sendRequest(RpcRequest request, long timeoutLength) {
        RpcFuture rpcFuture = new RpcFuture(request, timeoutLength);
        PendingRpcHolder.getPendingRPC().put(request.getRequestId(), rpcFuture);
        try {
            ChannelFuture channelFuture = channel.writeAndFlush(request).sync();
            if (!channelFuture.isSuccess()) {
                logger.error("Send request {} error.", request.getRequestId());
            }
        } catch (InterruptedException e) {
            logger.error("Send request exception: {}.", e.getMessage());
        }
        return rpcFuture;
    }

    public void setRpcMetaData(RpcMetaData rpcMetaData) {
        this.rpcMetaData = rpcMetaData;
    }

    /**
     * server端超时主动关闭
     * 触发client端重连 以此机制保持长链接
     * 主动关闭则不进行重连接
     * @param ctx
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (isIntentionalClose()) {
            super.channelInactive(ctx);
            return;
        }
        logger.info("Connection to server lose, active reconnect mechanism.");
        Connector connector = Connector.getInstance();
        try {
            connector.connectServerNode(rpcMetaData);
        } catch (Exception e) {
            connector.removeConnectRecord(rpcMetaData);
        }
    }


    private boolean isIntentionalClose() {
        return intentionalClose;
    }

    /**
     * 主动关闭时设置
     * @param intentionalClose boolean 是否主动关闭
     */
    public void setIntentionalClose(boolean intentionalClose) {
        this.intentionalClose = intentionalClose;
    }
}
