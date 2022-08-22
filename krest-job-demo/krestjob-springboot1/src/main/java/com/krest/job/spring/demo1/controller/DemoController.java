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

@KrestJobhandler /*添加 @KrestJobhandler 的接口，会被作为 JobHandler 注册到 admin 中 */
@RequestMapping(value = "/service")
@RestController
@Slf4j
public class DemoController {

    @Autowired
    KrestJobService krestJobService;

    /**
     * 注册定时任务类型
     * jobName：任务名称(同一个业务系统中需要保证唯一)
     * path: 远程调用的路径地址
     * method: 调用的方法类型，目前仅支持 Post
     * jobType: 任务类型：分片任务、普通定时任务
     * loadBalancerType ：负载均衡策略，支持：随机、轮询、权重
     */
    @KrestJobExecutor(
            jobName = "demo-job2",
            path = "service/demo-krestjob/sharding",
            method = MethodType.POST,
            jobType = JobType.SHARDING)
    @PostMapping("demo-krestjob/sharding")/* 目前仅支持 post 方法接口*/
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
