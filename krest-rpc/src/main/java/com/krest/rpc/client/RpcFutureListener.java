package com.krest.rpc.client;

public interface RpcFutureListener {

    /**
     * 成功返回结果
     * @param result
     */
    void onResult(Object result);

    /**
     * 发生了异常
     * @param throwable
     */
    void onException(Throwable throwable);
}
