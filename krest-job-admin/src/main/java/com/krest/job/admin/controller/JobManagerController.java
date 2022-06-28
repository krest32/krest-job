package com.krest.job.admin.controller;

import com.krest.job.admin.service.JobManagerService;
import com.krest.job.common.entity.JobHandler;
import com.krest.job.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("job/manager")
public class JobManagerController {


    @Autowired
    private JobManagerService jobManagerService;

    /**
     * 执行策略任务
     *
     * @param jobHandler
     * @return
     */
    @PostMapping("run/schedule")
    public R runScheduleJob(@RequestBody JobHandler jobHandler) {
        return jobManagerService.runScheduleJob(jobHandler);
    }


    /**
     * 执行策略任务
     *
     * @param jobHandler
     * @return
     */
    @PostMapping("stop/schedule")
    public R stopScheduleJob(@RequestBody JobHandler jobHandler) {
        return jobManagerService.stopScheduleJob(jobHandler);
    }
}
