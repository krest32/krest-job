package com.krest.job.admin.service;


import com.krest.job.common.entity.ServiceInfo;

import java.util.List;

public interface ServiceInfoService {

    boolean saveServiceInfo(ServiceInfo serviceInfo);

    ServiceInfo getService(String id);

    List<ServiceInfo> getSetviceList(String serviceName);

}
