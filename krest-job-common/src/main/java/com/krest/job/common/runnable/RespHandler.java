package com.krest.job.common.runnable;

import com.krest.job.common.entity.KrestJobFuture;
import com.krest.job.common.entity.KrestJobResponse;
import com.krest.job.common.executor.ThreadPoolConfig;
import com.krest.job.common.executor.ThreadPoolFactory;

import java.util.concurrent.*;

public class RespHandler {

    private ConcurrentMap<String, KrestJobFuture> krestJobFutureMap = new ConcurrentHashMap<>();
    private BlockingQueue<KrestJobResponse> responseQueue = new LinkedBlockingQueue<>();

    static ThreadPoolConfig poolConfig = new ThreadPoolConfig();
    static ThreadPoolExecutor respHandlerExecutor = ThreadPoolFactory.threadPoolExecutor(poolConfig);

    /**
     * 同时开启多个线程去处理结果
     */
    public RespHandler(int threads) {
        for (int i = 0; i < threads; i++) {
            respHandlerExecutor.execute(new RespHandleRunnable(krestJobFutureMap, responseQueue));
        }
    }

    // 将结果放入到 map 集合中
    public void register(String id, KrestJobFuture jobFuture) {
        krestJobFutureMap.put(id, jobFuture);
    }

    // 将最终的结果放入到队列当中
    public void addResponse(KrestJobResponse jobResponse) {
        responseQueue.add(jobResponse);
    }
}
