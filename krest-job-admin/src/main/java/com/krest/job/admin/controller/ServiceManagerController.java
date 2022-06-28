package com.krest.job.admin.controller;


import com.krest.job.admin.service.ServiceInfoService;
import com.krest.job.common.entity.ServiceInfo;
import com.krest.job.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("service")
public class ServiceManagerController {

    @Autowired
    ServiceInfoService serviceimpl;

    /**
     * 注册服务接口
     *
     * @param serviceInfo
     * @return
     */
    @PostMapping("register")
    public R registerService(@RequestBody ServiceInfo serviceInfo) {
        boolean flag = serviceimpl.saveServiceInfo(serviceInfo);
        return R.ok().data("saveSuccess", flag);
    }


    @GetMapping("get/service/{id}")
    public R getServiceByid(@PathVariable String id) {
        return R.ok().data("service", serviceimpl.getService(id));
    }


    @GetMapping("get/serviceList/{serviceName}")
    public R getServiceByName(@PathVariable String serviceName) {
        List<ServiceInfo> serviceList = serviceimpl.getSetviceList(serviceName);
        return R.ok().data("serviceList", serviceList);
    }
}
