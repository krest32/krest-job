package com.krest.job.common.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class KrestJobResponse {
    Integer code;
    Boolean status;
    String msg;
}
