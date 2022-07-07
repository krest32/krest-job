package com.krest.job.admin.balancer;

import com.krest.job.common.entity.ServiceInfo;

import java.util.List;
import java.util.Random;

public class ServiceInfoLoadBalancer {

    public static ServiceInfo randomServiceInfo(List<ServiceInfo> serviceInfos) {
        int total = serviceInfos.size();
        Random random = new Random();
        return serviceInfos.get(random.nextInt(total));
    }
}
