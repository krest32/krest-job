package com.krest.job.common.entity;

import com.krest.job.common.balancer.LoadBalancerType;
import lombok.Data;
import lombok.ToString;

/**
 * @author Administrator
 */
@Data
@ToString
public class JobHandler {
    String id;
    String appName;
    String path;
    String serviceAddress;
    String jobName;
    String jobGroup;
    JobType jobType;
    MethodType methodType;
    String args;
    String cron;
    Integer retryTimes;
    Integer appPos;
    boolean isRunning;
    LoadBalancerType loadBalanceType;
    String lastTriggerTime;
    String nextTriggerTime;
    String createTime;
}
