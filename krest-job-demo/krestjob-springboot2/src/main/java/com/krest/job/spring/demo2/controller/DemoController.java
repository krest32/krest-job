package com.krest.job.spring.demo2.controller;

import com.alibaba.fastjson.JSONObject;
import com.krest.job.common.balancer.LoadBalancerType;
import com.krest.job.common.entity.*;
import com.krest.job.core.annotation.KrestJobExecutor;
import com.krest.job.core.annotation.KrestJobhandler;
import com.krest.job.spring.starter.KrestJobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@KrestJobhandler
@RequestMapping(value = "/service")
@RestController
@Slf4j
public class DemoController {

    @Autowired
    KrestJobService krestJobService;

    @KrestJobExecutor(
            jobName = "demo-job1",
            path = "service/demo-krestjob",
            method = MethodType.GET,
            loadBalancerType = LoadBalancerType.WEIGHTROUNDRIBBON)
    @GetMapping("demo-krestjob")
    public KrestJobResponse demoNormalJob() {
        KrestJobResponse krestJobResponse = new KrestJobResponse();
        log.info("demo2 执行任务");
        return krestJobResponse;
    }

    @KrestJobExecutor(
            jobName = "demo-job2",
            path = "service/demo-krestjob/sharding",
            method = MethodType.POST,
            jobType = JobType.SHARDING)
    @PostMapping("demo-krestjob/sharding")
    public KrestJobResponse demoShardingJob(@RequestBody String requestStr) {
        ShardingJob shardingJob = JSONObject.parseObject(requestStr, ShardingJob.class);
        log.info("demo2 执行分片任务- total sharding: {}, local sharding id :{}, weight:{}"
                , shardingJob.getTotalSharding(), shardingJob.getShardingId(), shardingJob.getWeight());
        log.info("request data : {} ", shardingJob.getData());
        KrestJobResponse krestJobResponse = new KrestJobResponse();
        return krestJobResponse;
    }

    @KrestJobExecutor(
            jobName = "demo-job3",
            path = "service/demo-krestjob-post",
            method = MethodType.POST,
            loadBalancerType = LoadBalancerType.WEIGHTROUNDRIBBON)
    @PostMapping("demo-krestjob-post")
    public KrestJobResponse demoNormalJob(@RequestBody String requestStr) {
        KrestJobRequest krestJobRequest = JSONObject.parseObject(requestStr, KrestJobRequest.class);
        KrestJobResponse krestJobResponse = new KrestJobResponse();
        log.info("demo2 执行任务 post 任务");
        log.info(krestJobRequest.toString());
        krestJobResponse.setMsg("来自demo2的回执");
        return krestJobResponse;
    }
}
