package com.krest.rpc.common;

import lombok.Data;

/**
 * 返回结果构建
 */
@Data
public class RpcResponse {

    private int id;

    private Object result;

    private Throwable throwable;

    private boolean isInvokeSuccess;
}
