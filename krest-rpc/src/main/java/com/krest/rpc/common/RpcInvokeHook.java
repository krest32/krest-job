package com.krest.rpc.common;

public interface RpcInvokeHook {

    void beforeInvoke(String methodName, Object[] args);

    void afterInvoke(String methodName, Object[] args);
}
