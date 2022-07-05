package com.krest.job.common.utils;

import com.krest.job.common.entity.JobLog;
import com.krest.job.common.entity.KrestJobFuture;
import com.krest.job.common.entity.KrestJobResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class AnalyseUtil {

    public static boolean anaylise(List<KrestJobFuture> jobFutures, List<JobLog> jobLogs) {

        for (int i = 0; i < jobFutures.size(); i++) {
            KrestJobFuture jobFuture = jobFutures.get(i);
            JobLog jobLog = jobLogs.get(i);
            KrestJobResponse krestJobResponse;
            try {
                if (jobFuture.getTimeout() == 0) {
                    krestJobResponse = jobFuture.get();
                } else {
                    krestJobResponse = jobFuture.get(jobFuture.getTimeout());
                }
                log.info(Thread.currentThread().getName() + "任务执行结果:{}", krestJobResponse);
                //  如果失败， 记录异常信息
                if (!krestJobResponse.getStatus()) {
                    jobLog.setExceptionMsg(krestJobResponse.getThrowable() == null ? null : krestJobResponse.getThrowable().toString());
                    return false;
                }
                jobLog.setResultCode(krestJobResponse.getCode());
                jobLog.setResultMsg(krestJobResponse.getResult() == null
                        ? null : krestJobResponse.getResult().toString());
            } catch (Throwable throwable) {
                log.error(throwable.getMessage());
                jobLog.setExceptionMsg(throwable.getMessage());
                return false;
            }
        }
        return true;
    }
}
