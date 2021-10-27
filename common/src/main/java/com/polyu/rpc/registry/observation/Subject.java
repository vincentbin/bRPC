package com.polyu.rpc.registry.observation;

import com.polyu.rpc.info.RpcMetaData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;

import java.util.List;

/**
 * 观察者模式 事件源
 */
public interface Subject {

    /**
     * 注册观察者
     * @param observer 观察者
     */
    void registerObserver(Observer observer);

    /**
     * 通知观察者进行update
     * @param rpcMetaData rpc server信息
     * @param type zk 事件类型
     */
    void notifyObserver(List<RpcMetaData> rpcMetaData, PathChildrenCacheEvent.Type type);
}
