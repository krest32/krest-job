package com.krest.job.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Administrator
 */

@EnableConfigurationProperties
@EnableScheduling
@EnableAsync/*异步执行定时任务*/
@SpringBootApplication
@MapperScan("com.krest.job.admin.mapper")
public class KrestJobAdminApp {

    public static void main(String[] args) {
        SpringApplication.run(KrestJobAdminApp.class, args);

    }
}
