package com.krest.job.admin.balancer;

import com.krest.job.common.entity.ServiceInfo;

import java.util.List;
import java.util.Random;

/**
 * follower 调用策略
 */
public class ServiceInfoLoadBalancer {

    /**
     * 随机调用策略
     */
    public static ServiceInfo randomServiceInfo(List<ServiceInfo> serviceInfos) {
        int total = serviceInfos.size();
        Random random = new Random();
        return serviceInfos.get(random.nextInt(total));
    }
}
