package com.krest.job.admin.utils;


/**
 * @author: krest
 * @date: 2021/5/18 18:50
 * @description:
 */
public class ThreadPoolConfig {

    public Integer getCoreSize() {
        return coreSize;
    }

    public ThreadPoolConfig(Integer coreSize, Integer maxSize, Integer keepAliveTime) {
        this.coreSize = coreSize;
        this.maxSize = maxSize;
        this.keepAliveTime = keepAliveTime;
    }

    public void setCoreSize(Integer coreSize) {
        this.coreSize = coreSize;
    }

    public Integer getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }

    public Integer getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(Integer keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    Integer coreSize = 2;
    Integer maxSize = 5;
    Integer keepAliveTime = 3000;
}
