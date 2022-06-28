package com.krest.job.admin.controller;

import com.krest.job.admin.service.JobHandlerService;
import com.krest.job.common.entity.JobHandler;
import com.krest.job.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Administrator
 */

@Slf4j
@RestController
@RequestMapping("job/handler")
public class JobHandlerController {

    @Autowired
    private JobHandlerService jobHandlerService;

    /**
     * 注册 job handler
     *
     * @param jobHandler
     * @return
     */
    @PostMapping("registry")
    public R RegistryHobHandler(@RequestBody JobHandler jobHandler) {
        return jobHandlerService.registryJobHandler(jobHandler);
    }


    @GetMapping("search/{jobHandlerId}")
    public R searchJobHandlerById(@PathVariable String jobHandlerId){
        return jobHandlerService.searchJobHandlerById(jobHandlerId);
    }

}
