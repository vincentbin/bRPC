package com.polyu.rpc.client.result.future;


public interface AsyncRPCCallback {

    void success(Object result);

    void fail(Exception e);

}
