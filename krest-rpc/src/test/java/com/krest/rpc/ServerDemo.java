package com.krest.rpc;

import com.krest.rpc.common.RpcInvokeHook;
import com.krest.rpc.server.RpcServer;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class ServerDemo {
    @Test
    public void serverTest() {

        ServerTestInterface testInterface = new ServerTestInterface() {
            @Override
            public String testMethod() {
                return "我来自 server ";
            }
        };


        RpcInvokeHook hook = new RpcInvokeHook() {
            @Override
            public void beforeInvoke(String methodName, Object[] args) {
                log.info("beforeInvoke in server" + methodName);
            }

            @Override
            public void afterInvoke(String methodName, Object[] args) {
                log.info("afterInvoke in server" + methodName);
            }
        };


        RpcServer rpcServer = RpcServerBuilder.create()
                .serviceInterface(ServerTestInterface.class)
                .serviceProvider(testInterface)
                .threads(4)
                .hook(hook)
                .bind(3721)
                .build();
        rpcServer.start();
    }
}
