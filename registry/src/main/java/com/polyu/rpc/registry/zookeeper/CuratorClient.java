package com.polyu.rpc.registry.zookeeper;

import com.polyu.rpc.registry.RegistryConfigEnum;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;

import java.util.List;

/**
 * zk客户端
 */
public class CuratorClient {
    private CuratorFramework client;

    private CuratorClient(String connectString, String namespace, int sessionTimeout, int connectionTimeout) {
        client = CuratorFrameworkFactory.builder().namespace(namespace).connectString(connectString)
                .sessionTimeoutMs(sessionTimeout).connectionTimeoutMs(connectionTimeout)
                .retryPolicy(new ExponentialBackoffRetry(1000, 10))
                .build();
        client.start();
    }

    CuratorClient(String connectString, int timeout) {
        this(connectString, RegistryConfigEnum.ZK_NAME_SPACE.getValue(), timeout, timeout);
    }

    CuratorClient(String connectString) {
        this(
                connectString,
                RegistryConfigEnum.ZK_NAME_SPACE.getValue(),
                RegistryConfigEnum.ZK_SESSION_TIMEOUT.getTimeOutLength(),
                RegistryConfigEnum.ZK_CONNECTION_TIMEOUT.getTimeOutLength()
        );
    }

    void addConnectionStateListener(ConnectionStateListener connectionStateListener) {
        client.getConnectionStateListenable().addListener(connectionStateListener);
    }

    String createPathData(String path, byte[] data) throws Exception {
        return client.create().creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath(path, data);
    }

    void updatePathData(String path, byte[] data) throws Exception {
        client.setData().forPath(path, data);
    }

    void deletePath(String path) throws Exception {
        client.delete().forPath(path);
    }

    void watchNode(String path, Watcher watcher) throws Exception {
        client.getData().usingWatcher(watcher).forPath(path);
    }

    byte[] getData(String path) throws Exception {
        return client.getData().forPath(path);
    }

    List<String> getChildren(String path) throws Exception {
        return client.getChildren().forPath(path);
    }

    void watchTreeNode(String path, TreeCacheListener listener) {
        TreeCache treeCache = new TreeCache(client, path);
        treeCache.getListenable().addListener(listener);
    }

    void watchPathChildrenNode(String path, PathChildrenCacheListener listener) throws Exception {
        PathChildrenCache pathChildrenCache = new PathChildrenCache(client, path, true);
        pathChildrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        pathChildrenCache.getListenable().addListener(listener);
    }

    void close() {
        client.close();
    }
}
