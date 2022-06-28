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

    @GetMapping("run/direct/{jobHandlerId}")
    public R runJob(@PathVariable String jobHandlerId) {
        return jobManagerService.runJob(jobHandlerId);
    }


    @PostMapping("run/schedule")
    public R runJob(@RequestBody JobHandler jobHandler) {
        return jobManagerService.runScheduleJob(jobHandler);
    }


    @GetMapping("callback/{jobId}")
    public R callBack(@PathVariable String jobId) {
        return jobManagerService.callBack(jobId);
    }


    @GetMapping("stop/{jobId}")
    public R stop(@PathVariable String jobId) {
        return jobManagerService.stop(jobId);
    }

}
