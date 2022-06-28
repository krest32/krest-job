package com.krest.job.core.register;


import com.alibaba.fastjson.JSONObject;
import com.krest.job.core.annotation.KrestJobhandler;
import com.krest.job.core.config.CoreJobConfig;
import com.krest.job.core.annotation.KrestJobExecutor;
import com.krest.job.common.entity.JobHandler;
import com.krest.job.common.entity.MethodType;
import com.krest.job.common.utils.DateUtils;
import com.krest.job.common.utils.HttpUtil;
import com.krest.job.common.utils.IdWorker;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Method;


/**
 * @author Administrator
 */
@Component
@Slf4j
public class RegisterJobHandler implements BeanPostProcessor {

    IdWorker idWorker = new IdWorker();

    @Autowired
    CoreJobConfig coreJobConfig;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        // 获取被 @KrestJobHandler 注解的接口
        Class clazz = bean.getClass();
        if (clazz.isAnnotationPresent(KrestJobhandler.class)) {

            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(KrestJobExecutor.class)) {
                    // 生成 Job Handler 注册信息
                    JobHandler jobHandler = new JobHandler();
                    KrestJobExecutor krestJobExecutor = method.getDeclaredAnnotation(KrestJobExecutor.class);

                    jobHandler.setId(idWorker.nextId());
                    jobHandler.setAppName(coreJobConfig.getClientAppName());
                    jobHandler.setMethodType(getRequestMethodType(krestJobExecutor.method()));
                    jobHandler.setJobType(krestJobExecutor.jobType());
                    jobHandler.setPath(krestJobExecutor.path());
                    jobHandler.setCreateTime(DateUtils.getNowDate(DateUtils.getDateFormat1()));
                    jobHandler.setJobName(krestJobExecutor.jobName());
                    jobHandler.setJobGroup(krestJobExecutor.jobGroup());
                    jobHandler.setRunning(false);
                    jobHandler.setLoadBalanceType(krestJobExecutor.loadBalancerType());

                    // 注册 Job Handler 服务
                    String requestBodyJson = JSONObject.toJSONString(jobHandler);
                    String adminJobRegister = coreJobConfig.getAdminAddress() + "/job/handler/registry";

                    // 开始注册任务
                    log.info(requestBodyJson);
                    HttpUtil.postRequest(adminJobRegister, requestBodyJson);
                }
            }
        }
        return bean;
    }

    private String getRequestMethodType(MethodType method) {
        switch (method) {
            case GET:
                return "get";

            case PUT:
                return "put";
            case POST:
                return "post";

            case DELETE:
                return "delete";
            default:
                return "unknown";
        }
    }
}
