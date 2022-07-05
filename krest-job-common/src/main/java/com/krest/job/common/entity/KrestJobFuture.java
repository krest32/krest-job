package com.krest.job.common.entity;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@Data
@ToString
public class KrestJobFuture {
    // 远程调用的结果类型
    public final static int STATE_AWAIT = 0;
    public final static int STATE_SUCCESS = 1;

    // 等待调用结果
    private CountDownLatch countDownLatch;

    // 封装的结果
    KrestJobResponse result;
    String requestArgs;
    int timeout;
    int state;
    int id;

    // 异步调用结果接听起
    private KrestFutureListener krestFutureListener;

    public KrestJobFuture(int id, String requestArgs, int timeout) {
        this.id = id;
        this.requestArgs = requestArgs;
        countDownLatch = new CountDownLatch(1);
        state = STATE_AWAIT;
    }

    /**
     * 获取异步调用的结果，但是该方法会阻塞
     */
    public KrestJobResponse get() throws Throwable {
        // 等待结果
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }

        if (state != STATE_AWAIT) {
            return result;
        } else {
            throw new RuntimeException(KrestJobMessage.KrestJobFutureRunException);
        }
    }


    /**
     * 等待多长时间然后获取结果
     */
    public KrestJobResponse get(int timeout) throws Throwable {
        boolean awaitSuccess = true;

        try {
            awaitSuccess = countDownLatch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }

        if (!awaitSuccess) {
            throw new RuntimeException();
        }

        if (state == STATE_SUCCESS) {
            return result;
        } else {
            throw new RuntimeException("RpcFuture Exception!");
        }
    }

    /**
     * 设置结果
     */
    public void setResult(KrestJobResponse result) {
        this.result = result;
        state = STATE_SUCCESS;
        if (krestFutureListener != null) {
            krestFutureListener.onResult(result);
        }
        countDownLatch.countDown();
    }


    /**
     * 判断当前任务是否处理完成
     */
    public boolean isDone() {
        return state != STATE_AWAIT;
    }


    /**
     * 设置 Future 监听器
     */
    public void setRpcFutureListener(KrestFutureListener krestFutureListener) {
        this.krestFutureListener = krestFutureListener;
    }
}
