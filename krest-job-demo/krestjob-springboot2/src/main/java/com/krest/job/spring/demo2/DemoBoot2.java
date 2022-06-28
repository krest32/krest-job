package com.krest.job.spring.demo2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@ComponentScan(basePackages = "com.krest.job")
public class DemoBoot2 {
    public static void main(String[] args) {
        SpringApplication.run(DemoBoot2.class, args);
    }
}
