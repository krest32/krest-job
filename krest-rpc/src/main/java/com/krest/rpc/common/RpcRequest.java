package com.krest.rpc.common;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 请求样式
 */
@Data
@AllArgsConstructor
public class RpcRequest {

    int id;

    String methodName;

    /**
     * 请求参数
     */
    Object[] args;

}
