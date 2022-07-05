package com.krest.job.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
public class KrestJobResponse {
    int id;
    Integer code;
    Boolean status;
    String msg;
    Object result;
    Throwable throwable;
}
