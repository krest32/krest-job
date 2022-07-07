package com.krest.job.admin.schedule;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.krest.job.admin.cache.LocalCache;
import com.krest.job.admin.config.AdminProperties;
import com.krest.job.admin.mapper.ServiceInfoMapper;
import com.krest.job.admin.mapper.ServiceLockMapper;
import com.krest.job.common.entity.*;
import com.krest.job.common.utils.DateUtil;
import com.krest.job.common.utils.HttpUtil;
import com.krest.job.common.utils.JobIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CheckService {

    final String heartBeatPath = "/service/heartbeat";
    final String updateServiceStatusPath = "/service/update/status";
    final String clientHeartBeatPath = "/client/detect/service";

    @Autowired
    AdminProperties adminProperties;

    @Autowired
    ServiceInfoMapper serviceInfoMapper;

    @Autowired
    ServiceLockMapper serviceLockMapper;

    /**
     * 每一段时间进行探测， 同时删除已经死亡的 JobHandler 服务
     */
    @Scheduled(cron = "0/10 * * * * ?")
    public void detectService() {
        // 检测 Job Handler 的工作由 leader 来完成
        if (LocalCache.getCurServiceInfo().getServiceRole().equals(ServiceType.LEADER)) {
            QueryWrapper<ServiceInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("service_role", ServiceType.JOBHANDLER);
            List<ServiceInfo> serviceInfos = serviceInfoMapper.selectList(queryWrapper);

            // 初始化設置
            LocalCache.setServiceInfos(serviceInfos);
            List<KrestJobFuture> jobFutures = new ArrayList<>();
            for (ServiceInfo serviceInfo : serviceInfos) {
                String url = serviceInfo.getServiceAddress() + clientHeartBeatPath;
                KrestJobRequest jobRequest = new KrestJobRequest(
                        UUID.randomUUID().toString(),
                        null, url, MethodType.POST);
                KrestJobFuture jobFuture = HttpUtil.doRequest(jobRequest);
                jobFutures.add(jobFuture);
            }

            // 更新 serviceInfos 信息
            List<String> urls = checkJobFuture(jobFutures, serviceInfos);

            if (urls.size() > 0) {
                serviceInfoMapper.deleteBatchIds(urls);
                LocalCache.setServiceInfos(serviceInfos.stream().filter(serviceInfo ->
                        urls.contains(serviceInfo.getId())).collect(Collectors.toList()));
            }
        }
    }


    /**
     * 检测 follower
     * 1. 发送心跳报文
     * 2. 删除未能正确链接的service
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void detectFollower() {
        // 如果检测自己为 leader
        if (LocalCache.getCurServiceInfo().getServiceRole().equals(ServiceType.LEADER)) {
            QueryWrapper<ServiceInfo> followerQuery = new QueryWrapper<>();
            followerQuery.eq("service_role", ServiceType.FOLLOWER);
            List<ServiceInfo> followerInfos = serviceInfoMapper.selectList(followerQuery);

            QueryWrapper<ServiceInfo> observerQuery = new QueryWrapper<>();
            observerQuery.eq("service_role", ServiceType.OBSERVER);
            List<ServiceInfo> observerInfos = serviceInfoMapper.selectList(observerQuery);
            log.info("followers num : {} ", followerInfos.size() + observerInfos.size());
            // 发送心跳包, 同时删除不能正确链接的 service
            sendHeartBeat(followerInfos);
            sendHeartBeat(observerInfos);
        }
    }


    /**
     * 开启所有服务的同步状态
     */
    public boolean synchAdminService() throws Throwable {
        log.info("krest job admin servers go into synch mode");
        // 找到所有不是 job handle 的 service
        QueryWrapper<ServiceInfo> infoQueryWrapper = new QueryWrapper<>();
        infoQueryWrapper.ne("service_role", ServiceType.JOBHANDLER);
        List<ServiceInfo> serviceInfos = serviceInfoMapper.selectList(infoQueryWrapper);

        // 更新数据库所有 service 状态为 observer
        serviceInfos.stream().map(temp -> {
            temp.setServiceRole(ServiceType.OBSERVER);
            serviceInfoMapper.updateById(temp);
            return temp;
        }).collect(Collectors.toList());


        // 遍历 list 查看是否能够找到 leader
        ServiceInfo leaderService = null;
        for (ServiceInfo temp : serviceInfos) {
            if (temp.getServiceRole().equals(ServiceType.LEADER)) {
                log.info("找到 Leader : {} ", leaderService);
                leaderService = temp;
                break;
            }
        }

        // 如果检测到了 leader 信息， 就发送心跳报文
        if (null != leaderService) {
            // 发送心跳包
            String leaderUrl = leaderService.getServiceAddress() + heartBeatPath;
            KrestJobRequest jobRequest = new KrestJobRequest(
                    UUID.randomUUID().toString(), null, leaderUrl, MethodType.POST);

            int tryCnt = 0;
            // 重试
            while (tryCnt < 3) {
                KrestJobFuture jobFuture = HttpUtil.doRequest(jobRequest);
                KrestJobResponse response = jobFuture.get();
                if (response.getStatus()) {
                    log.info("心跳检测成功,lead-url:{}", leaderUrl);
                    return true;
                }
                tryCnt++;
                Thread.sleep(2000);
            }
        }
        // 开始尝试获取锁，注册新的 leader
        return generateNewLeader();
    }


    /**
     * 生成新的 leader service
     */
    private boolean generateNewLeader() throws Throwable {
        // 尝试获取锁
        ServiceLock serviceLock = new ServiceLock();
        serviceLock.setServiceAddress(LocalCache.getCurServiceInfo().getServiceAddress());
        int holdLock = serviceLockMapper.updateToLock(serviceLock);

        // 如果得到锁，那么推荐自己为 leader
        if (holdLock == 1) {
            QueryWrapper<ServiceInfo> infoQueryWrapper = new QueryWrapper<>();
            infoQueryWrapper.ne("service_role", ServiceType.JOBHANDLER);
            List<ServiceInfo> serviceInfos = serviceInfoMapper.selectList(infoQueryWrapper);
            serviceInfos.stream().map(temp -> {
                if (temp.getServiceAddress().equals(LocalCache.getCurServiceInfo().getServiceAddress())) {
                    temp.setServiceRole(ServiceType.LEADER);
                    LocalCache.setCurServiceInfo(temp);
                    log.info("设置新的leader : {}", temp.getServiceAddress());
                    // 更新数据库
                    temp.setUpdateTime(DateUtil.getNowDate(DateUtil.getDateFormat1()));
                    serviceInfoMapper.updateById(LocalCache.getCurServiceInfo());
                } else {
                    temp.setServiceRole(ServiceType.FOLLOWER);
                    // 更新其他服务的状态为 follower
                    KrestJobRequest request = new KrestJobRequest(
                            UUID.randomUUID().toString(), JSONObject.toJSONString(temp),
                            temp.getServiceAddress() + updateServiceStatusPath, MethodType.POST
                    );
                    HttpUtil.doRequest(request);
                    // 更新数据库
                    temp.setUpdateTime(DateUtil.getNowDate(DateUtil.getDateFormat1()));
                    serviceInfoMapper.updateById(temp);
                }
                return temp;
            }).collect(Collectors.toList());

            // 解开当前锁
            serviceLockMapper.updateToUnLock(new ServiceLock(
                    null, false, null,
                    DateUtil.getNowDate(DateUtil.getDateFormat1())));

            return true;
        } else {

            // 如果没有得到锁，那么说明其他线程正在注册 leader, 循环发送探测报文
            if (sendDetectInfoToLeader())
                return true;

            // 探测现在上锁的 service
            List<ServiceLock> serviceLocks = serviceLockMapper.selectList(null);

            if (null != serviceLocks && serviceLocks.size() > 0) {
                ServiceLock lock = serviceLocks.get(0);
                // 如果还是锁定状态
                if (lock.isLock()) {
                    String remoteUrl = lock.getServiceAddress() + heartBeatPath;
                    KrestJobRequest request = new KrestJobRequest(
                            UUID.randomUUID().toString(), null, remoteUrl, MethodType.POST
                    );

                    KrestJobFuture jobFuture = HttpUtil.doRequest(request);
                    KrestJobResponse response = jobFuture.get();

                    // 如果服务在存活状态
                    if (response.getStatus()) {
                        // 开始解锁
                        lock.setLock(false);
                    } else {
                        // 如果服务已经死亡将该服务从 serviceInfo 中删除
                        QueryWrapper<ServiceInfo> infoQuery = new QueryWrapper<>();
                        infoQuery.eq("service_address", lock.getServiceAddress());
                        serviceInfoMapper.delete(infoQuery);
                    }

                    // 开始解锁
                    lock.setServiceAddress(null);
                    lock.setStartTime(null);
                    serviceLockMapper.updateToUnLock(lock);
                }
            }
            // 再次进入生成 Leader 的状态
            return generateNewLeader();
        }
    }

    /**
     * 查看数据库是否有 leader 的信息
     */
    private boolean sendDetectInfoToLeader() throws InterruptedException {
        int tryCnt = 0;
        while (tryCnt < 3) {
            QueryWrapper<ServiceInfo> adminQuery = new QueryWrapper<>();
            adminQuery.eq("service_role", ServiceType.LEADER);
            List<ServiceInfo> leaders = serviceInfoMapper.selectList(adminQuery);
            if (null != leaders && leaders.size() > 0) {
                log.info("得到 Leader : " + leaders.get(0).getServiceAddress());
                return true;
            }
            Thread.sleep(1000);
            tryCnt++;
        }
        return false;
    }


    private void sendHeartBeat(List<ServiceInfo> serviceInfos) {
        List<KrestJobFuture> jobFutures = new ArrayList<>();
        // 逐个发送心跳报文
        for (int i = 0; i < serviceInfos.size(); i++) {
            ServiceInfo serviceInfo = serviceInfos.get(i);
            KrestJobRequest request = new KrestJobRequest();
            request.setId(UUID.randomUUID().toString());
            request.setTargetUrl(serviceInfo.getServiceAddress() + heartBeatPath);
            request.setArgs(JSONObject.toJSONString(LocalCache.getCurServiceInfo()));
            request.setMethodType(MethodType.POST);
            KrestJobFuture jobFuture = HttpUtil.doRequest(request);
            // 记录结果
            jobFutures.add(jobFuture);
        }

        for (int i = 0; i < jobFutures.size(); i++) {
            try {
                KrestJobFuture jobFuture = jobFutures.get(i);
                KrestJobResponse response = jobFuture.get();
                ServiceInfo curService = serviceInfos.get(i);
                if (response.getStatus()) {
                    // 更新为 观察这状态的 服务
                    if (curService.getServiceRole().equals(ServiceType.OBSERVER)) {
                        curService.setServiceRole(ServiceType.FOLLOWER);
                        serviceInfoMapper.updateById(curService);
                    }
                    continue;
                } else {
                    // 再次 发送 心跳报文
                    KrestJobRequest request = new KrestJobRequest();
                    request.setId(UUID.randomUUID().toString());
                    request.setTargetUrl(curService.getServiceAddress() + heartBeatPath);
                    request.setArgs(JSONObject.toJSONString(LocalCache.getCurServiceInfo()));
                    request.setMethodType(MethodType.POST);
                    KrestJobFuture curJobFuture = HttpUtil.doRequest(request);
                    // 如果仍然无法链接
                    if (!curJobFuture.get().getStatus()) {
                        serviceInfoMapper.deleteById(curService.getId());
                    }
                    log.info("admin follower 无法链接, 删除 follower 信息 ：" + curService.getServiceAddress());
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }


    private List<String> checkJobFuture(List<KrestJobFuture> jobFutures, List<ServiceInfo> serviceInfos) {
        List<String> urlIdList = new ArrayList<>();
        for (int i = 0; i < jobFutures.size(); i++) {
            KrestJobResponse krestJobResponse;
            try {
                krestJobResponse = jobFutures.get(i).get();
                if (!krestJobResponse.getStatus()) {
                    log.warn("服务:{} ,已经死亡,移除该服务", serviceInfos.get(i).getServiceAddress());
                    urlIdList.add(serviceInfos.get(i).getId());
                } else {
                    ServiceInfo serviceInfo = serviceInfos.get(i);
                    serviceInfo.setUpdateTime(DateUtil.getNowDate(DateUtil.getDateFormat1()));
                    serviceInfoMapper.updateById(serviceInfo);
                }
            } catch (Throwable throwable) {
                log.error(throwable.getMessage());
            }
        }
        return urlIdList;
    }
}
