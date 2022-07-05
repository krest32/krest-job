package com.krest.job.spring.starter.register;


import com.krest.job.common.executor.ThreadPoolConfig;
import com.krest.job.common.executor.ThreadPoolFactory;
import com.krest.job.core.annotation.KrestJobExecutor;
import com.krest.job.core.annotation.KrestJobhandler;
import com.krest.job.core.config.CoreJobConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author Administrator
 */
@Component
@Slf4j
public class RegisterJobHandler implements BeanPostProcessor {

    ThreadPoolConfig poolConfig = new ThreadPoolConfig();

    ThreadPoolExecutor executor = ThreadPoolFactory.threadPoolExecutor(poolConfig);

    private AtomicInteger idGenerate = new AtomicInteger(0);




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
                    RegisterRunnable registerRunnable = new RegisterRunnable(method, coreJobConfig, idGenerate.getAndAdd(1));
                    executor.execute(registerRunnable);
                }
            }
        }
        return bean;
    }


}
