package com.krest.job.common.executor;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolFactory {
    public static ThreadPoolExecutor threadPoolExecutor(ThreadPoolConfig config) {
        return new ThreadPoolExecutor(
                config.coreSize,
                config.maxSize,
                config.keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(100),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
    }
}
