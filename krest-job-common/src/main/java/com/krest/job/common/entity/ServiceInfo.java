package com.krest.job.common.entity;

import lombok.Data;
import lombok.ToString;


@Data
@ToString
public class ServiceInfo {
    String id;
    String appName;
    String serviceAddress;
    String weight;
    String createTime;
    String updateTime;
}
