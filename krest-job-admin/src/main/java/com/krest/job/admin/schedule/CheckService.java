package com.krest.job.admin.schedule;


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
import java.util.stream.Collectors;

@Slf4j
@Component
public class CheckService {

    final String detectPath = "/detect/service";
    final String heartBeatPath = "/service/heartbeat";

    @Autowired
    AdminProperties adminProperties;

    @Autowired
    ServiceInfoMapper serviceInfoMapper;

    @Autowired
    ServiceLockMapper serviceLockMapper;

    /**
     * 每一段时间进行探测， 同时删除已经死亡的 JobHandler 服务
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void detectService() {
        QueryWrapper<ServiceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("service_role", ServiceType.JOBHANDLER);
        List<ServiceInfo> serviceInfos = serviceInfoMapper.selectList(queryWrapper);

        // 初始化設置
        LocalCache.setServiceInfos(serviceInfos);
        List<KrestJobFuture> jobFutures = new ArrayList<>();
        for (ServiceInfo serviceInfo : serviceInfos) {
            String url = serviceInfo.getServiceAddress() + heartBeatPath;
            KrestJobRequest jobRequest = new KrestJobRequest(JobIdGenerator.getNextJobId(),
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


    /**
     * 每一段时间进行探测， 同时删除已经死亡的 Follower 服务
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void detectFollower() {

        if (LocalCache.getCurServiceInfo().getServiceRole().equals(ServiceType.LEADER)) {
            QueryWrapper<ServiceInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("service_role", ServiceType.FOLLOWER);
            List<ServiceInfo> serviceInfos = serviceInfoMapper.selectList(queryWrapper);

            LocalCache.setAdminServiceInfos(serviceInfos);
            List<KrestJobFuture> jobFutures = new ArrayList<>();
            for (ServiceInfo serviceInfo : serviceInfos) {
                String url = serviceInfo.getServiceAddress() + detectPath;
                KrestJobRequest jobRequest = new KrestJobRequest(JobIdGenerator.getNextJobId(),
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
     * 每一段时间进行探测， 同时删除已经死亡的 JobHandler 服务
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void detectAdminServer() throws Throwable {
        // 找到所有不知 job handle 的 service
        QueryWrapper<ServiceInfo> infoQueryWrapper = new QueryWrapper<>();
        infoQueryWrapper.ne("service_role", ServiceType.JOBHANDLER);
        List<ServiceInfo> serviceInfos = serviceInfoMapper.selectList(infoQueryWrapper);


        // 如果本机是 Leader，就检测心跳，删除无法连接的 service
        ServiceInfo localService = LocalCache.getCurServiceInfo();
        if (localService.getServiceRole().equals(ServiceType.LEADER)) {
            // 发送心跳包
            log.info("开始发送心跳包");
            sendHeartBeat(serviceInfos);
            return;
        }

        // 遍历 list 找到 leader
        ServiceInfo leaderService = null;
        for (ServiceInfo temp : serviceInfos) {
            if (temp.getServiceRole().equals(ServiceType.LEADER)) {
                leaderService = temp;
                break;
            }
        }

        log.info("找到Leader");
        if (null != leaderService) {
            // 发送心跳包
            String leaderUrl = leaderService.getServiceAddress() + heartBeatPath;
            KrestJobRequest jobRequest = new KrestJobRequest(
                    JobIdGenerator.getNextJobId(), null, leaderUrl, MethodType.POST);

            int tryCnt = 0;
            while (tryCnt < 3) {
                KrestJobFuture jobFuture = HttpUtil.doRequest(jobRequest);
                KrestJobResponse krestJobResponse = jobFuture.get();
                if (krestJobResponse.getStatus()) {
                    log.info("心跳检测成功,lead-url:{}", leaderUrl);
                    return;
                }
                tryCnt++;
                Thread.sleep(2000);
            }
            generateNewLeader(serviceInfos);
        } else {
            generateNewLeader(serviceInfos);
        }
    }

    private void sendHeartBeat(List<ServiceInfo> serviceInfos) {
        List<KrestJobFuture> jobFutures = new ArrayList<>();
        // 逐个发送心跳报文
        for (int i = 0; i < serviceInfos.size(); i++) {
            ServiceInfo serviceInfo = serviceInfos.get(i);
            if (!serviceInfo.getServiceRole().equals(ServiceType.LEADER)) {
                KrestJobRequest request = new KrestJobRequest();
                request.setId(JobIdGenerator.getNextJobId());
                request.setTargetUrl(serviceInfo.getServiceAddress() + heartBeatPath);
                request.setMethodType(MethodType.POST);
                KrestJobFuture jobFuture = HttpUtil.doRequest(request);
                jobFutures.add(jobFuture);
            }
        }

        // 检测心跳报文的结果
        List<ServiceInfo> prepareDelService = new ArrayList<>();
        for (int i = 0; i < jobFutures.size(); i++) {
            KrestJobFuture jobFuture = jobFutures.get(i);
            try {
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
                    request.setId(JobIdGenerator.getNextJobId());
                    request.setTargetUrl(curService.getServiceAddress() + heartBeatPath);
                    request.setMethodType(MethodType.POST);
                    KrestJobFuture curJobFuture = HttpUtil.doRequest(request);
                    // 如果仍然无法链接
                    if (!curJobFuture.get().getStatus()) {
                        serviceInfoMapper.deleteById(curService.getId());
                    }
                }
                prepareDelService.add(serviceInfos.get(i));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }


    /**
     * 生成新的 leader
     */
    private void generateNewLeader(List<ServiceInfo> serviceInfos) {
        ServiceLock serviceLock = new ServiceLock();
        serviceLock.setServiceAddress(LocalCache.getCurServiceInfo().getServiceAddress());
        int holdLock = serviceLockMapper.updateToLock(serviceLock);
        // 添加锁
        if (holdLock == 1) {
            serviceInfos.stream().map(temp -> {
                if (temp.getServiceAddress().equals(LocalCache.getCurServiceInfo().getServiceAddress())) {
                    temp.setServiceRole(ServiceType.LEADER);
                    LocalCache.setCurServiceInfo(temp);
                    log.info("设置新的leader:{}", temp);
                } else {
                    temp.setServiceRole(ServiceType.FOLLOWER);
                }
                serviceInfoMapper.updateById(temp);
                return temp;
            }).collect(Collectors.toList());
            serviceLockMapper.updateToUnLock(new ServiceLock(
                    null, false, null,
                    DateUtil.getNowDate(DateUtil.getDateFormat1())));
            // 等待其
        } else {
            int tryCnt = 0;
            while (tryCnt < 3) {
                QueryWrapper<ServiceInfo> adminQuery = new QueryWrapper<>();
                adminQuery.eq("service_role", ServiceType.LEADER);
                List<ServiceInfo> leaders = serviceInfoMapper.selectList(adminQuery);
                if (null != leaders && leaders.size() >= 1) {
                    log.info("得到Leader:" + leaders);
                    return;
                }
                tryCnt++;
            }
            generateNewLeader(serviceInfos);
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
