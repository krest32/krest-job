package com.krest.job.common.entity;

public interface KrestFutureListener {

    void onResult(Object object);

    void onException(Throwable throwable);
}
