package com.krest.job.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceLock {
    String id;
    boolean isLock;
    String serviceAddress;
    String startTime;
}
