package com.krest.rpc.client;

import com.krest.rpc.common.RpcInvokeHook;
import com.krest.rpc.common.RpcRequest;
import com.krest.rpc.netty.config.NettyKryoDecoder;
import com.krest.rpc.netty.config.NettyKryoEncoder;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import io.netty.channel.*;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 远程通信时，采用生成代理对象的方式
 */
@Slf4j
public class RpcClient implements InvocationHandler {

    private long timeoutMills = 0;
    private RpcInvokeHook rpcInvokeHook = null;
    private String host;
    private int port;

    private RpcClientResponseHandler rpcClientResponseHandler;
    private AtomicInteger invokeIdGenerator = new AtomicInteger(0);

    /**
     * netty 相关配置
     */
    private Bootstrap bootstrap;
    private Channel channel;
    private RpcClientChannelInactiveListener rpcClientChannelInactiveListener;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 执行远程调用
        RpcFuture rpcFuture = call(method.getName(), args);
        if (rpcFuture == null) {
            log.info("RpcClient is unavailable when disconnect with the server.");
            return null;
        }

        // 处理返回的结果
        Object result;
        if (timeoutMills == 0) {
            result = rpcFuture.get();
        } else {
            result = rpcFuture.get(timeoutMills);
        }

        if (rpcInvokeHook != null) {
            rpcInvokeHook.afterInvoke(method.getName(), args);
        }
        return result;
    }

    /**
     * 构造方法，同时与 server 建立远程链接
     */
    protected RpcClient(long timeoutMills, RpcInvokeHook rpcInvokeHook, String host, int port,
                        int threads) {
        this.timeoutMills = timeoutMills;
        this.rpcInvokeHook = rpcInvokeHook;
        this.host = host;
        this.port = port;

        rpcClientResponseHandler = new RpcClientResponseHandler(threads);

        rpcClientChannelInactiveListener = new RpcClientChannelInactiveListener() {
            @Override
            public void onInactive() {
                log.info("connection with server is closed.");
                log.info("try to reconnect to the server.");
                channel = null;
                do {
                    channel = tryConnect();
                }
                while (channel == null);
            }
        };
    }

    /**
     * 重新链接
     *
     * @return
     */
    private io.netty.channel.Channel tryConnect() {
        try {
            log.info("Try to connect to [" + host + ":" + port + "].");
            // channel 同步结果绑定
            ChannelFuture future = bootstrap.connect(host, port).sync();
            if (future.isSuccess()) {
                log.info("Connect to [" + host + ":" + port + "] successed.");
                return future.channel();
            } else {
                // 如果链接失败，那么重新尝试
                log.info("Connect to [" + host + ":" + port + "] failed.");
                log.info("Try to reconnect in 10s.");
                Thread.sleep(10000);
                return null;
            }
        } catch (Exception exception) {
            log.error("Connect to [" + host + ":" + port + "] failed.");
            log.info("Try to reconnect in 10 seconds.");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
            return null;
        }
    }


    /**
     * 执行方法，代理对象的方法，等待返回的结果
     *
     * @return
     */
    public RpcFuture call(String methodName, Object... args) {
        if (rpcInvokeHook != null) {
            rpcInvokeHook.beforeInvoke(methodName, args);
        }

        RpcFuture rpcFuture = new RpcFuture();
        int id = invokeIdGenerator.addAndGet(1);
        rpcClientResponseHandler.register(id, rpcFuture);

        RpcRequest rpcRequest = new RpcRequest(id, methodName, args);
        if (channel != null) {
            channel.writeAndFlush(rpcRequest);
        } else {
            return null;
        }

        return rpcFuture;
    }

    public void connect() {
        bootstrap = new Bootstrap();
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline().addLast(new NettyKryoDecoder(),
                                    new RpcClientDispatchHandler(rpcClientResponseHandler, rpcClientChannelInactiveListener),
                                    new NettyKryoEncoder());
                        }
                    });
            bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);

            do {
                channel = tryConnect();
            }
            while (channel == null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
