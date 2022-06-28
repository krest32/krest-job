package com.krest.job.admin.utils;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ShardingJob {
    Integer start;
    Integer end;
}
