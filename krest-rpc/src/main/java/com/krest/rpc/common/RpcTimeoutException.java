package com.krest.rpc.common;

public class RpcTimeoutException extends Throwable {
    private static final long serialVersionUID = -3399060930740626516L;

    public RpcTimeoutException() {
        super("time out when calling a Rpc Invoke!");
    }

}
