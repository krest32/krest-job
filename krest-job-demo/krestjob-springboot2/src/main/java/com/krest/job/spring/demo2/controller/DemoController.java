package com.krest.job.spring.demo2.controller;

import com.krest.job.common.entity.JobType;
import com.krest.job.core.annotation.KrestJobExecutor;
import com.krest.job.core.annotation.KrestJobhandler;
import com.krest.job.common.entity.MethodType;
import com.krest.job.spring.demo2.entity.ShardingJob;
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

    @KrestJobExecutor(path = "service/demo-krestjob", method = MethodType.GET)
    @GetMapping("demo-krestjob")
    public String demokrestjob() {
        System.out.println("demo2 执行任务");
        return krestJobService.sayHello();
    }

    @KrestJobExecutor(path = "service/demo-krestjob/sharding", method = MethodType.POST, jobType = JobType.SHARDING)
    @PostMapping("demo-krestjob/sharding")
    public String demokrestjob(@RequestBody ShardingJob shardingJob) {
        log.info("demo1 执行分片任务- start:{}, end :{}", shardingJob.getStart(), shardingJob.getEnd());
        Integer result = 0;
        for (int i = shardingJob.getStart(); i < shardingJob.getEnd(); i++) {
            result += i;
        }
        System.out.println(result);
        return String.valueOf(result);
    }
}
