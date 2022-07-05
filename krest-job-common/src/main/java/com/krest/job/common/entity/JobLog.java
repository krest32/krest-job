package com.krest.job.common.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class JobLog {

    String logId;
    String jobId;
    String batchId;
    String runApp;
    Integer retryCount;
    Integer resultCode;
    String requestArgs;
    String resultMsg;
    String exceptionMsg;
    String createTime;

}
