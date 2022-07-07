package com.krest.job.spring.starter.register;

import com.alibaba.fastjson.JSONObject;
import com.krest.job.common.entity.*;
import com.krest.job.common.utils.DateUtil;
import com.krest.job.common.utils.HttpUtil;
import com.krest.job.common.utils.IdWorker;
import com.krest.job.core.annotation.KrestJobExecutor;
import com.krest.job.spring.starter.KrestJobConfig;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.UUID;

@Slf4j
public class RegisterRunnable implements Runnable {

    Method method;
    KrestJobConfig coreJobConfig;

    public RegisterRunnable(Method method, KrestJobConfig KrestJobConfig) {
        this.coreJobConfig = KrestJobConfig;
        this.method = method;
    }

    @Override
    public void run() {
        JobHandler jobHandler = new JobHandler();
        KrestJobExecutor krestJobExecutor = method.getDeclaredAnnotation(KrestJobExecutor.class);


        jobHandler.setId(IdWorker.nextId());
        jobHandler.setAppName(coreJobConfig.getClient_app_name());
        jobHandler.setMethodType(getRequestMethodType(krestJobExecutor.method()));
        jobHandler.setJobType(krestJobExecutor.jobType());
        jobHandler.setPath(krestJobExecutor.path());
        jobHandler.setCreateTime(DateUtil.getNowDate(DateUtil.getDateFormat1()));
        jobHandler.setJobName(krestJobExecutor.jobName());
        jobHandler.setJobGroup(krestJobExecutor.jobGroup());
        jobHandler.setRunning(false);
        jobHandler.setLoadBalanceType(krestJobExecutor.loadBalancerType());

        // 注册 Job Handler 服务
        String requestBodyJson = JSONObject.toJSONString(jobHandler);


        for (int i = 0; i < coreJobConfig.getAdmin_address().size(); i++) {
            String adminJobRegister = coreJobConfig.getAdmin_address().get(i) + "/job/handler/registry";
            KrestJobRequest krestJobRequest = new KrestJobRequest(
                    UUID.randomUUID().toString(), requestBodyJson,
                    adminJobRegister, MethodType.POST);
            KrestJobFuture jobFuture;
            while (true) {
                try {
                    jobFuture = HttpUtil.doRequest(krestJobRequest);
                    KrestJobResponse jobResponse = jobFuture.get();
                    if (jobResponse != null && jobResponse.getStatus() == true) {
                        log.info(KrestJobMessage.RegisterJobHandlerSuccess);
                        break;
                    }

                } catch (Exception e) {
                    log.error(KrestJobMessage.RegisterJobHandlerFailed);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }

                try {
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    private MethodType getRequestMethodType(MethodType method) {
        switch (method) {
            default:
                return MethodType.POST;
        }
    }
}
