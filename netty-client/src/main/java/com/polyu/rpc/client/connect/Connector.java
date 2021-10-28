package com.polyu.rpc.client.connect;

import com.polyu.rpc.client.netty.RpcClientInitializer;
import com.polyu.rpc.client.netty.handler.RpcClientHandler;
import com.polyu.rpc.info.RpcMetaData;
import com.polyu.rpc.info.RpcServiceInfo;
import com.polyu.rpc.route.MetaDataKeeper;
import com.polyu.rpc.util.ThreadPoolUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.NettyRuntime;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

@Data
public class Connector {
    private static final Logger logger = LoggerFactory.getLogger(Connector.class);

    /**
     * client建立连线程池
     */
    private ThreadPoolExecutor connectionThreadPool = ThreadPoolUtil.makeThreadPool(4, 8, 600L);

    private Map<RpcMetaData, RpcClientHandler> connectedServerNodes = new ConcurrentHashMap<>();
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(NettyRuntime.availableProcessors() / 2);
    private static volatile Connector connector;

    public static Connector getInstance() {
        if (connector == null) {
            synchronized (Connector.class) {
                if (connector == null) {
                    connector = new Connector();
                }
            }
        }
        return connector;
    }

    /**
     * 连接peer host
     * @param rpcMetaData peer server 元信息
     */
    public void connectServerNode(RpcMetaData rpcMetaData) {
        if (rpcMetaData.getServiceInfoList() == null || rpcMetaData.getServiceInfoList().isEmpty()) {
            logger.info("No service on node, host: {}, port: {}.", rpcMetaData.getHost(), rpcMetaData.getPort());
            return;
        }
        ConnectUpdater.getInstance().getRpcMetaDataSet().add(rpcMetaData);
        logger.info("New service node, host: {}, port: {}.", rpcMetaData.getHost(), rpcMetaData.getPort());
        for (RpcServiceInfo serviceProtocol : rpcMetaData.getServiceInfoList()) {
            logger.info("New service info, name: {}, version: {}.", serviceProtocol.getServiceName(), serviceProtocol.getVersion());
        }
        final InetSocketAddress remotePeer = new InetSocketAddress(rpcMetaData.getHost(), rpcMetaData.getPort());
        connectionThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                Bootstrap b = new Bootstrap();
                b.group(eventLoopGroup)
                        .channel(NioSocketChannel.class)
                        .handler(new RpcClientInitializer());

                ChannelFuture channelFuture = b.connect(remotePeer);
                channelFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(final ChannelFuture channelFuture) {
                        if (channelFuture.isSuccess()) {
                            logger.info("Successfully connect to remote server, remote peer = {}.", remotePeer);
                            RpcClientHandler rpcClientHandler = channelFuture.channel().pipeline().get(RpcClientHandler.class);
                            connectedServerNodes.put(rpcMetaData, rpcClientHandler);
                            rpcClientHandler.setRpcMetaData(rpcMetaData);
                            rpcClientHandler.setIntentionalClose(false);
                            // 方便后续快速选择 在此记录
                            MetaDataKeeper.addZkChild(rpcMetaData);
                            HandlerManager.signalAvailableHandler();
                        } else {
                            // 失败进行回收
                            removeConnectRecord(rpcMetaData);
                            logger.error("Can not connect to remote server, remote peer = {}.", remotePeer);
                        }
                    }
                });
            }
        });
    }

    /**
     * 关闭 & 移除 连接
     * @param rpcMetaData peer server 信息
     */
    public void removeAndCloseHandler(RpcMetaData rpcMetaData) {
        RpcClientHandler handler = connectedServerNodes.get(rpcMetaData);
        removeConnectRecord(rpcMetaData);
        if (handler != null) {
            handler.setIntentionalClose(true);
            handler.close();
        }
    }

    /**
     * 连接失败 移除连接记录
     * @param rpcMetaData server information
     */
    public void removeConnectRecord(RpcMetaData rpcMetaData) {
        ConnectUpdater.getInstance().getRpcMetaDataSet().remove(rpcMetaData);
        connectedServerNodes.remove(rpcMetaData);
        MetaDataKeeper.removeZkChild(rpcMetaData);
        logger.info("Remove one connection, host: {}, port: {}.", rpcMetaData.getHost(), rpcMetaData.getPort());
    }

}
