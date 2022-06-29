package com.krest.rpc.server;

import com.krest.rpc.common.RpcInvokeHook;
import com.krest.rpc.common.RpcRequestWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 处理客户端发来的请求
 */
@Slf4j
public class RpcServerRequestHandler {
    private Class<?> interfaceClass;
    private Object serviceProvider;
    private RpcInvokeHook rpcInvokeHook;

    private int threads;
    private ExecutorService threadPool;
    private BlockingQueue<RpcRequestWrapper> requestQueue = new LinkedBlockingQueue<RpcRequestWrapper>();

    public RpcServerRequestHandler(Class<?> interfaceClass, Object serviceProvider, int threads,
                                   RpcInvokeHook rpcInvokeHook) {
        this.interfaceClass = interfaceClass;
        this.serviceProvider = serviceProvider;
        this.threads = threads;
        this.rpcInvokeHook = rpcInvokeHook;
    }

    public void start() {
        threadPool = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            threadPool.execute(new RpcServerRequestHandleRunnable(interfaceClass,
                    serviceProvider, rpcInvokeHook, requestQueue));
        }
    }

    public void addRequest(RpcRequestWrapper rpcRequestWrapper) {
        try {
            requestQueue.put(rpcRequestWrapper);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
