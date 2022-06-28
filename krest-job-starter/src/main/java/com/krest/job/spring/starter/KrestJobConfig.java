package com.krest.job.spring.starter;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "krest.job")
public class KrestJobConfig {


    private String admin_address;

    private String weight;

    private String client_app_name;

    private String client_address;
}
