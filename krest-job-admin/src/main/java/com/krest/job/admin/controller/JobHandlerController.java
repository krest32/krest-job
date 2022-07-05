package com.krest.job.admin.controller;

import com.alibaba.fastjson.JSONObject;
import com.krest.job.admin.service.JobHandlerService;
import com.krest.job.common.entity.JobHandler;
import com.krest.job.common.entity.KrestJobMessage;
import com.krest.job.common.entity.KrestJobRequest;
import com.krest.job.common.entity.KrestJobResponse;
import com.krest.job.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 调度服务
 *
 * @author Administrator
 */

@Slf4j
@RestController
@RequestMapping("job/handler")
public class JobHandlerController {

    @Autowired
    JobHandlerService jobHandlerService;

    /**
     * 注册 job handler
     */
    @PostMapping("registry")
    public KrestJobResponse registryHobHandler(@RequestBody String requestStr) {

        KrestJobRequest request = JSONObject.parseObject(requestStr, KrestJobRequest.class);
        JobHandler jobHandler = JSONObject.parseObject(request.getArgs(), JobHandler.class);
        jobHandlerService.registryJobHandler(jobHandler);

        KrestJobResponse response = new KrestJobResponse(
                request.getId(), 200, true,
                KrestJobMessage.RegisterJobHandlerSuccess,
                "测试123", null
        );

        return response;
    }


    /**
     * 查找 job handler 的信息
     */
    @GetMapping("search/{jobHandlerId}")
    public R searchJobHandlerById(@PathVariable String jobHandlerId) {
        return jobHandlerService.searchJobHandlerById(jobHandlerId);
    }
}
