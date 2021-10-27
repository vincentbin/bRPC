package com.polyu.rpc.registry.observation;

import com.polyu.rpc.info.RpcMetaData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;

import java.util.List;

/**
 * 观察者模式 观察者
 */
public interface Observer {

    /**
     * 观察者进行服务更新
     * @param rpcMetaData rpc server信息
     * @param type zk 事件类型
     */
    void update(List<RpcMetaData> rpcMetaData, PathChildrenCacheEvent.Type type);

}
