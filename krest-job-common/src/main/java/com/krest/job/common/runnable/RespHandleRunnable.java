package com.krest.job.common.runnable;

import com.krest.job.common.entity.KrestJobFuture;
import com.krest.job.common.entity.KrestJobResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class RespHandleRunnable implements Runnable {

    private ConcurrentMap<String, KrestJobFuture> jobFutureConcurrentMap;
    private BlockingQueue<KrestJobResponse> responseQueue;

    /**
     * 构造方法
     */
    public RespHandleRunnable(
            ConcurrentMap<String, KrestJobFuture> jobFutureConcurrentMap,
            BlockingQueue<KrestJobResponse> responseQueue) {

        this.jobFutureConcurrentMap = jobFutureConcurrentMap;
        this.responseQueue = responseQueue;

    }

    @Override
    public void run() {
        while (true) {
            try {
                // 从队列中获取请求结果
                KrestJobResponse jobResponse = responseQueue.take();
                // 得到结果
                String id = jobResponse.getId();
                KrestJobFuture jobFuture = jobFutureConcurrentMap.remove(id);
                // 将调用的结果放入到 Future 调用的结果当中
                jobFuture.setResult(jobResponse);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
