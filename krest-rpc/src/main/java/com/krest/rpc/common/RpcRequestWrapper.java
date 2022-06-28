package com.krest.rpc.common;

import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RpcRequestWrapper {
    private final RpcRequest rpcRequest;
    private final Channel channel;

    /**
     * 获取被调用方法的 id
     * @return
     */
    public int getId() {
        return rpcRequest.getId();
    }

    /**
     * 获取被调用方法的名称
     * @return
     */
    public String getMethodName() {
        return rpcRequest.getMethodName();
    }

    /**
     * 获取被调用方法的参数
     * @return
     */
    public Object[] getArgs() {
        return rpcRequest.getArgs();
    }
}
