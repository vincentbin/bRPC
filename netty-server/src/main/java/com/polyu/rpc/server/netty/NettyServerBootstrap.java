package com.polyu.rpc.server.netty;

import com.polyu.rpc.registry.ServiceRegistry;
import com.polyu.rpc.util.ThreadPoolUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

@NoArgsConstructor
public class NettyServerBootstrap implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(NettyServerBootstrap.class);

    private int CORE_THREAD_POOL_SIZE = 35;
    private int MAX_THREAD_POOL_SIZE = 70;

    private String serverAddress;

    private ServiceRegistry serviceRegistry;

    private Map<String, Object> serviceKey2BeanMap;

    private ThreadPoolExecutor businessTaskThreadPool;

    NettyServerBootstrap(
            int corePoolSize, int maxPoolSize,
            String serverName,
            String serverAddress,
            Map<String, Object> serviceKey2BeanMap,
            ServiceRegistry serviceRegistry) {
        this.serverAddress = serverAddress;
        this.serviceKey2BeanMap = serviceKey2BeanMap;
        this.serviceRegistry = serviceRegistry;
        this.CORE_THREAD_POOL_SIZE = corePoolSize;
        this.MAX_THREAD_POOL_SIZE = maxPoolSize;
        this.businessTaskThreadPool = ThreadPoolUtil.makeServerThreadPool(
                serverName,
                CORE_THREAD_POOL_SIZE,
                MAX_THREAD_POOL_SIZE
        );
    }

    NettyServerBootstrap(
            String serverName,
            String serverAddress,
            Map<String, Object> serviceKey2BeanMap,
            ServiceRegistry serviceRegistry) {
        this.serverAddress = serverAddress;
        this.serviceKey2BeanMap = serviceKey2BeanMap;
        this.serviceRegistry = serviceRegistry;
        this.businessTaskThreadPool = ThreadPoolUtil.makeServerThreadPool(
                serverName,
                CORE_THREAD_POOL_SIZE,
                MAX_THREAD_POOL_SIZE
        );
    }

    /**
     * 异步启动netty server
     */
    @Override
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new RpcServerInitializer(serviceKey2BeanMap, businessTaskThreadPool))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            String[] array = serverAddress.split(":");
            String host = array[0];
            int port = Integer.parseInt(array[1]);
            ChannelFuture future = bootstrap.bind(port).sync();
            // 服务注册
            registerService(host, port);
            logger.info("Server started on port {}.", port);
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                logger.info("Rpc server remoting server stop.");
            } else {
                logger.error("Rpc server remoting server error", e);
            }
        } finally {
            try {
                assert serviceRegistry != null;
                serviceRegistry.unregisterService();
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    /**
     * 服务注册
     * @param host 主机地址
     * @param port 端口号
     * @throws Exception
     */
    private void registerService(String host, int port) throws Exception {
        if (serviceRegistry == null) {
            logger.warn("serviceRegistry is null.");
            return;
        }
        try {
            serviceRegistry.registerService(host, port, serviceKey2BeanMap);
        } catch (Exception e) {
            logger.error("Rpc server register server failed. host: {}, port: {}", host, port);
            throw new Exception("register failed.");
        }
    }
}
