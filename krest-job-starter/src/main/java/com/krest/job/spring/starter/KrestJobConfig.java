package com.krest.job.spring.starter;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "krest.job")
public class KrestJobConfig {

    private List<String> admin_address;

    private String weight;

    private String client_app_name;

    private String client_address;
}
