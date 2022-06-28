package com.krest.job.core.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Data
@Configuration
public class CoreJobConfig {

    @Value("${krest.job.client_app_name}")
    String clientAppName;


    @Value("${krest.job.admin_address}")
    String adminAddress;

}
