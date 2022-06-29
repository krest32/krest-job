package com.krest.rpc.client;

import com.krest.rpc.common.RpcInvokeHook;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Proxy;

public class RpcClientProxyBuilder {
    /**
     * 内部类
     * @param <T>
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProxyBuilder<T> {

        private Class<T> clazz;
        private RpcClient rpcClient;

        private long timeoutMills = 0;
        private RpcInvokeHook rpcInvokeHook = null;
        private String host;
        private int port;
        private int threads;

        private ProxyBuilder(Class<T> clazz) {
            this.clazz = clazz;
        }

        public ProxyBuilder<T> timeout(long timeoutMills) {
            this.timeoutMills = timeoutMills;
            if (timeoutMills < 0) {
                throw new IllegalArgumentException("timeoutMills can not be minus!");
            }
            return this;
        }

        public ProxyBuilder<T> hook(RpcInvokeHook hook) {
            this.rpcInvokeHook = hook;
            return this;
        }

        public ProxyBuilder<T> connect(String host, int port) {
            this.host = host;
            this.port = port;
            return this;
        }
        public ProxyBuilder<T> threads(int threadCount) {
            this.threads = threadCount;
            return this;
        }

        @SuppressWarnings("unchecked")
        public T build() {
            if (threads <= 0) {
                threads = Runtime.getRuntime().availableProcessors();
            }
            rpcClient = new RpcClient(timeoutMills, rpcInvokeHook, host, port, threads);
            rpcClient.connect();

            return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, rpcClient);
        }

        /**
         * build the asynchronous proxy.In asynchronous way, a RpcFuture will be
         * return immediately.
         */
        public RpcClientAsyncProxy buildAsyncProxy() {
            if (threads <= 0) {
                threads = Runtime.getRuntime().availableProcessors();
            }

            rpcClient = new RpcClient(timeoutMills, rpcInvokeHook, host, port, threads);
            rpcClient.connect();

            return new RpcClientAsyncProxy(rpcClient, clazz);
        }
    }

    public static <T> ProxyBuilder<T> create(Class<T> targetClass) {
        return new ProxyBuilder<T>(targetClass);
    }

}
