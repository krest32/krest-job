package com.krest.job.common.executor;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: krest
 * @date: 2021/5/18 18:50
 * @description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThreadPoolConfig {
    Integer coreSize = 4;
    Integer maxSize = 8;
    Integer keepAliveTime = 3 * 60 * 1000;

}
