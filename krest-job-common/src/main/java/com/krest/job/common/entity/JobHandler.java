package com.krest.job.common.entity;

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
    String jobType;
    String methodType;
    String args;
    String cron;
    String lastTriggerTime;
    String nextTriggerTime;
    String createTime;
}
