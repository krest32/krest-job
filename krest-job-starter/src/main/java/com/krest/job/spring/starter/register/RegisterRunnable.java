package com.krest.job.spring.starter.register;

import com.alibaba.fastjson.JSONObject;
import com.krest.job.common.entity.*;
import com.krest.job.common.utils.DateUtil;
import com.krest.job.common.utils.HttpUtil;
import com.krest.job.common.utils.IdWorker;
import com.krest.job.core.annotation.KrestJobExecutor;
import com.krest.job.core.config.CoreJobConfig;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@Slf4j
public class RegisterRunnable implements Runnable {

    Method method;
    CoreJobConfig coreJobConfig;
    Integer id;

    public RegisterRunnable(Method method, CoreJobConfig coreJobConfig, Integer id) {
        this.coreJobConfig = coreJobConfig;
        this.method = method;
        this.id = id;
    }

    @Override
    public void run() {
        JobHandler jobHandler = new JobHandler();
        KrestJobExecutor krestJobExecutor = method.getDeclaredAnnotation(KrestJobExecutor.class);

        jobHandler.setId(IdWorker.nextId());
        jobHandler.setAppName(coreJobConfig.getClientAppName());
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
        String adminJobRegister = coreJobConfig.getAdminAddress() + "/job/handler/registry";

        KrestJobRequest krestJobRequest = new KrestJobRequest(
                this.id, requestBodyJson,
                adminJobRegister, MethodType.POST);

        KrestJobFuture jobFuture;
        System.out.println(krestJobRequest);

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

    private MethodType getRequestMethodType(MethodType method) {
        switch (method) {
            default:
                return MethodType.POST;
        }
    }
}
