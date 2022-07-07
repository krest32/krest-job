package com.krest.job.common.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class JobIdGenerator {

    static volatile AtomicInteger idGen = new AtomicInteger(0);

    public static int getNextJobId() {
        System.out.println(idGen.get());
        return idGen.addAndGet(1);
    }
}
