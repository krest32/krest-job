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
    String jobName;
    String jobGroup;
    String jobType;
    String methodType;
    String args;
    String cron;
    Integer appPos;
    boolean isRunning;
    LoadBalancerType loadBalanceType;
    String lastTriggerTime;
    String nextTriggerTime;
    String createTime;
}
