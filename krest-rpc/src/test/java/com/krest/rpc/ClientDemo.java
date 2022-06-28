package com.krest.rpc;

import com.krest.rpc.client.RpcClientProxyBuilder;
import com.krest.rpc.common.RpcInvokeHook;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class ClientDemo {

    @Test
    public void client() {

        RpcInvokeHook hook = new RpcInvokeHook() {
            @Override
            public void beforeInvoke(String methodName, Object[] args) {
                log.info("方法调用前执行");
            }

            @Override
            public void afterInvoke(String methodName, Object[] args) {
                log.info("方法调用后执行");
            }
        };

        // 构建一个远程链接对象
        ClientTestInterface testInterface = RpcClientProxyBuilder.create(ClientTestInterface.class)
                .timeout(0)
                .threads(4)
                .hook(hook)
                .connect("127.0.0.1", 3721)
                .build();

        for (int i = 0; i < 10; i++) {
            System.out.println("invoke result = " + testInterface.testMethod());
        }

    }
}
