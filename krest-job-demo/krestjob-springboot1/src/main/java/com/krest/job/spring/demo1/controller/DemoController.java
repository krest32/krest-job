package com.krest.job.spring.demo1.controller;

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
            jobName = "demo-job2",
            path = "service/demo-krestjob/sharding",
            method = MethodType.POST,
            jobType = JobType.SHARDING)
    @PostMapping("demo-krestjob/sharding")
    public String demoShardingJob(@RequestBody String requestStr) throws InterruptedException {
        System.out.println(requestStr);
        KrestJobRequest krestJobRequest = JSONObject.parseObject(requestStr, KrestJobRequest.class);
        ShardingJob shardingJob = JSONObject.parseObject(krestJobRequest.getArgs(), ShardingJob.class);

        log.info("demo2 执行分片任务- total sharding: {}, local sharding id :{}, weight:{}"
                , shardingJob.getTotalSharding(), shardingJob.getShardingId(), shardingJob.getWeight());

        String responseStr = JSONObject.toJSONString(
                new KrestJobResponse(krestJobRequest.getId(),
                        200, true, "success",
                        null, null));
        log.info("request data : {} ", shardingJob.getData());
        Thread.sleep(3000);

        return responseStr;
    }

    @KrestJobExecutor(
            jobName = "demo-job3",
            path = "service/demo-krestjob-post",
            method = MethodType.POST,
            loadBalancerType = LoadBalancerType.WEIGHTROUNDRIBBON)
    @PostMapping("demo-krestjob-post")
    public String demoNormalJob(@RequestBody String requestStr) {
        KrestJobRequest krestJobRequest = JSONObject.parseObject(requestStr, KrestJobRequest.class);
        log.info("demo2 执行任务 post 任务");
        log.info(krestJobRequest.toString());
        String responseStr = JSONObject.toJSONString(
                new KrestJobResponse(krestJobRequest.getId(),
                        200, true, "success",
                        null, null));
        log.info("request data : {} ", requestStr);

        return responseStr;
    }
}
