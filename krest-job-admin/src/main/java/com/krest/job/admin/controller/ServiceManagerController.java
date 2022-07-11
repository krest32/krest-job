package com.krest.job.admin.controller;


import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.krest.job.admin.cache.LocalCache;
import com.krest.job.admin.mapper.ServiceInfoMapper;
import com.krest.job.admin.schedule.CheckService;
import com.krest.job.admin.service.ServiceInfoService;
import com.krest.job.common.entity.KrestJobRequest;
import com.krest.job.common.entity.KrestJobResponse;
import com.krest.job.common.entity.ServiceInfo;
import com.krest.job.common.entity.ServiceType;
import com.krest.job.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


/**
 * 服务管理接口
 */
@Slf4j
@RestController
@RequestMapping("service")
public class ServiceManagerController {

    @Autowired
    ServiceInfoService serviceimpl;

    @Autowired
    ServiceInfoMapper serviceInfoMapper;

    @Autowired
    CheckService checkService;

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
        return R.ok().data("service", serviceInfoMapper.selectById(id));
    }


    @GetMapping("get/serviceList/{serviceName}")
    public R getServiceByName(@PathVariable String serviceName) {
        List<ServiceInfo> serviceList = serviceimpl.getSetviceList(serviceName);
        return R.ok().data("serviceList", serviceList);
    }

    /**
     * 注册Follower
     */
    @PostMapping("register/follower")
    public String registerFollower(@RequestBody String requestStr) {
        KrestJobRequest request = JSONObject.parseObject(requestStr, KrestJobRequest.class);
        ServiceInfo serviceInfo = JSONObject.parseObject(request.getArgs(), ServiceInfo.class);
        KrestJobResponse response;

        if (null != serviceInfo && StringUtils.isEmpty(serviceInfo.getServiceAddress())) {
            List<ServiceInfo> adminServiceInfos = LocalCache.getAdminServiceInfos()
                    .stream()
                    .filter(tempInfo ->
                            !tempInfo.getServiceAddress().equals(serviceInfo.getServiceAddress()))
                    .collect(Collectors.toList());
            adminServiceInfos.add(serviceInfo);
            LocalCache.setAdminServiceInfos(adminServiceInfos);
            response = new KrestJobResponse(
                    request.getId(), 200, true,
                    "跟随成功", null, null);
        } else {
            response = new KrestJobResponse(
                    request.getId(), 200, true,
                    "跟随失败", null, null);
        }
        return JSONObject.toJSONString(response);
    }

    @PostMapping("get/status")
    public String getServiceRole() {
        return LocalCache.getCurServiceInfo().getServiceRole().toString();
    }

    @PostMapping("heartbeat")
    public String heartbeat(@RequestBody String requestStr) {
        KrestJobRequest krestJobRequest = JSONObject.parseObject(requestStr, KrestJobRequest.class);
        ServiceInfo serviceInfo = JSONObject.parseObject(krestJobRequest.getArgs(), ServiceInfo.class);
        log.info("心跳检测 from :" + serviceInfo.getServiceAddress());
        LocalCache.setExpireTime(System.currentTimeMillis() + 50 * 1000);
        KrestJobResponse response = new KrestJobResponse(
                krestJobRequest.getId(), 200, true,
                "心跳检测成功",
                "follower address : " + LocalCache.getCurServiceInfo().getServiceAddress(),
                null);
        return JSONObject.toJSONString(response);
    }

    @PostMapping("update/status")
    public String updateStatus(@RequestBody String requestStr) throws Throwable {
        // 解析得到 service 信息
        KrestJobRequest request = JSONObject.parseObject(requestStr, KrestJobRequest.class);
        ServiceInfo serviceInfo = JSONObject.parseObject(request.getArgs(), ServiceInfo.class);

        // 更改为 service 状态
        if (serviceInfo.getServiceRole().equals(ServiceType.FOLLOWER)) {
            LocalCache.getCurServiceInfo().setServiceRole(ServiceType.FOLLOWER);
        } else if (serviceInfo.getServiceRole().equals(ServiceType.OBSERVER)) {
            LocalCache.getCurServiceInfo().setServiceRole(ServiceType.OBSERVER);
            checkService.synchAdminService();
        } else {

        }

        KrestJobResponse response = new KrestJobResponse(
                request.getId(), 200, true,
                "更新状态成功", null, null);
        return JSONObject.toJSONString(response);
    }


}
