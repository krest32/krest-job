package com.krest.rpc.client;

import com.krest.rpc.common.RpcTimeoutException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RpcFuture {
    // 结果代码
    public final static int STATE_AWAIT = 0;
    public final static int STATE_SUCCESS = 1;
    public final static int STATE_EXCEPTION = 2;

    // 信号量
    private CountDownLatch countDownLatch;
    // 返回的结果
    private Object result;
    private Throwable throwable;
    private int state;
    private RpcFutureListener rpcFutureListener = null;

    public RpcFuture() {
        countDownLatch = new CountDownLatch(1);
        state = STATE_AWAIT;
    }

    public Object get() throws Throwable {
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (state == STATE_SUCCESS) {
            return result;
        } else if (state == STATE_EXCEPTION) {
            throw throwable;
            //should not run to here!
        } else {
            throw new RuntimeException("RpcFuture Exception!");
        }
    }

    public Object get(long timeout) throws Throwable {
        boolean awaitSuccess = true;
        try {
            awaitSuccess = countDownLatch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!awaitSuccess) {
            throw new RpcTimeoutException();
        }

        if (state == STATE_SUCCESS) {
            return result;
        } else if (state == STATE_EXCEPTION) {
            throw throwable;
            //should not run to here!
        } else {
            throw new RuntimeException("RpcFuture Exception!");
        }
    }

    public void setResult(Object result) {
        this.result = result;
        state = STATE_SUCCESS;

        if (rpcFutureListener != null) {
            rpcFutureListener.onResult(result);
        }

        countDownLatch.countDown();
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
        state = STATE_EXCEPTION;

        if (rpcFutureListener != null) {
            rpcFutureListener.onException(throwable);
        }
        countDownLatch.countDown();
    }

    public boolean isDone() {
        return state != STATE_AWAIT;
    }

    public void setRpcFutureListener(RpcFutureListener rpcFutureListener) {
        this.rpcFutureListener = rpcFutureListener;
    }

}
