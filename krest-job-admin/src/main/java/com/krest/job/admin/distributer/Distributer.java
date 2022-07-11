package com.krest.job.admin.distributer;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.krest.job.admin.balancer.ServiceInfoLoadBalancer;
import com.krest.job.admin.cache.LocalCache;
import com.krest.job.admin.mapper.ServiceInfoMapper;
import com.krest.job.common.entity.JobHandler;
import com.krest.job.common.entity.ServiceInfo;
import com.krest.job.common.entity.ServiceType;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


/**
 * follower 调度器
 */
@Slf4j
@Component
public class Distributer {

    OkHttpClient okHttpClient = new OkHttpClient();

    @Autowired
    ServiceInfoMapper serviceInfoMapper;


    /**
     * 从 follower 中选择合适执行器
     * 1. 如果是 leader, 那么就对任务直接进行调度
     * 2. 如果是 follower，需要将信息转发到 leader, 然后由 leader 进行调度
     */
    public void releaseTasK(JobHandler jobHandler) {
        // 如果是 Leader， 直接获取 follower 进行调度
        if (LocalCache.getCurServiceInfo().getServiceRole().equals(ServiceType.LEADER)) {
            String runJobPath = "/job/manager/run";
            QueryWrapper<ServiceInfo> followerQuery = new QueryWrapper<>();
            followerQuery.eq("service_role", ServiceType.FOLLOWER);
            List<ServiceInfo> followers = serviceInfoMapper.selectList(followerQuery);
            // 如果有大于0个以上的follower，那么就在follower上运行任务，否则在leader上运行任务
            ServiceInfo curService;
            if (null != followers && followers.size() > 0) {
                curService = ServiceInfoLoadBalancer.randomServiceInfo(followers);
            } else {
                curService = LocalCache.getCurServiceInfo();
            }
            if (runJobAtService(jobHandler, curService, runJobPath)) {
                log.info("转发任务成功:{}", jobHandler.getJobName());
            } else {
                log.info("转发任务失败:{}", jobHandler.getJobName());
            }

        } else {
            // 如果是 follower, 就需要将 job handler 信息转发到 leader
            String distributePath = "/job/manager/run/schedule";
            QueryWrapper<ServiceInfo> leaderQuery = new QueryWrapper<>();
            leaderQuery.eq("service_role", ServiceType.LEADER);
            ServiceInfo leaderService = serviceInfoMapper.selectOne(leaderQuery);
            log.info("转发任务志 Leader ");
            if (runJobAtService(jobHandler, leaderService, distributePath)) {
                log.info("转发任务成功:{}", jobHandler.getJobName());
            } else {
                log.info("转发任务失败:{}", jobHandler.getJobName());

            }

        }

    }

    private boolean runJobAtService(JobHandler jobHandler, ServiceInfo serviceInfo, String jobPath) {
        Response response = null;
        try {
            RequestBody body = RequestBody.create(JSONObject.toJSONString(jobHandler),
                    MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url(serviceInfo.getServiceAddress() + jobPath)
                    .post(body)
                    .build();
            Call call = okHttpClient.newCall(request);
            response = call.execute();
            if (response.isSuccessful()) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return false;
        } finally {
            if (null != response) {
                response.close();
            }
        }
    }
}
