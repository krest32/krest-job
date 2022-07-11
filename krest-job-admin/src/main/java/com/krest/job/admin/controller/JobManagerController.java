package com.krest.job.admin.controller;

import com.krest.job.admin.distributer.Distributer;
import com.krest.job.admin.mapper.ServiceInfoMapper;
import com.krest.job.admin.service.JobManagerService;
import com.krest.job.common.entity.JobHandler;
import com.krest.job.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 任务调度接口
 */
@Slf4j
@RestController
@RequestMapping("job/manager")
public class JobManagerController {

    OkHttpClient okHttpClient = new OkHttpClient();


    @Autowired
    private JobManagerService jobManagerService;

    @Autowired
    ServiceInfoMapper serviceInfoMapper;

    @Autowired
    Distributer distributer;

    /**
     * 开始执行 策略任务
     */
    @PostMapping("run")
    public R runJob(@RequestBody JobHandler jobHandler) {
        return jobManagerService.runScheduleJob(jobHandler);
    }

    /**
     * 开始执行 策略任务
     */
    @PostMapping("run/schedule")
    public R runScheduleJob(@RequestBody JobHandler jobHandler) {
        distributer.releaseTasK(jobHandler);
        return R.ok();
    }


    /**
     * 停止执行 策略任务
     */
    @PostMapping("stop/schedule")
    public R stopScheduleJob(@RequestBody JobHandler jobHandler) {
        return jobManagerService.stopScheduleJob(jobHandler);
    }

    @GetMapping("hello")
    public String hello() {
        return "hello";
    }
}
