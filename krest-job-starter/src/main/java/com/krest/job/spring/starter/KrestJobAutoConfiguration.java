package com.krest.job.spring.starter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KrestJobConfig.class)
public class KrestJobAutoConfiguration {

    private KrestJobConfig jobConfig;

    public KrestJobAutoConfiguration(KrestJobConfig jobConfig) {
        this.jobConfig = jobConfig;
    }

    /**
     * 实例化 KrestJobService并载入Spring IoC容器
     */
    @Bean
    @ConditionalOnMissingBean
    public KrestJobService krestJobService() {
        // 在这个方法中，可以实现注册服务的方法
        KrestJobService krestJobService = new KrestJobService(this.jobConfig);
        krestJobService.registerService();
        return krestJobService;
    }


}
