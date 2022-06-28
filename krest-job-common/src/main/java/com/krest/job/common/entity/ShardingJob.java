package com.krest.job.common.entity;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class ShardingJob {

    Integer shardingId;
    Integer weight;
    Integer totalSharding;
    List<String> data;

}
