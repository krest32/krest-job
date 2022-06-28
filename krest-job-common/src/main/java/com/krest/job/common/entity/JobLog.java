package com.krest.job.common.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class JobLog {

    String logId;
    String jobId;
    String runApp;
    Integer retryCount;
    Integer resultCode;
    String resultMsg;
    String createTime;

}
