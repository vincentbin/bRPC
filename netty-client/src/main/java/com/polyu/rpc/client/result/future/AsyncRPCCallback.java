package com.polyu.rpc.client.result.future;

/**
 * callback process
 */
public interface AsyncRPCCallback {

    /**
     * success invoke
     * @param result result
     */
    void success(Object result);

    /**
     * failure invoke
     * @param e exception
     */
    void fail(Exception e);
}
