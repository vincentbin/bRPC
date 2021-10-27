package com.polyu.rpc.info;

import com.polyu.rpc.util.JsonUtil;
import lombok.Data;

import java.util.List;
import java.util.Objects;

/**
 * 服务注册、订阅内容载体
 */
@Data
public class RpcMetaData {

    private String host;
    private int port;
    /**
     * 服务列表list
     */
    private List<RpcServiceInfo> serviceInfoList;

    public String toJson() {
        return JsonUtil.objectToJson(this);
    }

    public static RpcMetaData fromJson(String json) {
        return JsonUtil.jsonToObject(json, RpcMetaData.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RpcMetaData that = (RpcMetaData) o;
        return port == that.port &&
                Objects.equals(host, that.host) &&
                isListEquals(serviceInfoList, that.getServiceInfoList());
    }

    private boolean isListEquals(List<RpcServiceInfo> thisList, List<RpcServiceInfo> thatList) {
        if (thisList == null && thatList == null) {
            return true;
        }
        if (thisList == null || thatList == null || thisList.size() != thatList.size()) {
            return false;
        }
        return thisList.containsAll(thatList) && thatList.containsAll(thisList);
    }

    /**
     * 服务列表不一样 即使ip & port相同也不属于同一个zk节点
     * @return
     */
    @Override
    public int hashCode() {
        return Objects.hash(host, port, serviceInfoList.hashCode());
    }

    @Override
    public String toString() {
        return toJson();
    }

    public List<RpcServiceInfo> getServiceInfoList() {
        return serviceInfoList;
    }

    public void setServiceInfoList(List<RpcServiceInfo> serviceInfoList) {
        this.serviceInfoList = serviceInfoList;
    }
}
