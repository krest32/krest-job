package com.krest.job.admin.schedule;


import com.krest.job.admin.mapper.ServiceInfoMapper;
import com.krest.job.common.entity.ServiceInfo;
import com.krest.job.common.utils.DateUtils;
import com.krest.job.common.utils.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class CheckServiceAlive {
    final String dateFormat = "yyyy-MM-dd HH:mm:ss";
    final String path = "/detect/service";


    @Autowired
    ServiceInfoMapper serviceInfoMapper;

    /**
     * 每 30s 进行一次探测
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void detectService() {
        List<ServiceInfo> serviceInfos = serviceInfoMapper.selectList(null);
        for (ServiceInfo serviceInfo : serviceInfos) {
            String url = serviceInfo.getServiceAddress() + path;
            boolean flag = HttpUtil.getRequest(url);
            if (!flag) {
                log.warn("服务:{} ,已经死亡,移除该服务", serviceInfo.getServiceAddress());
                serviceInfoMapper.deleteById(serviceInfo.getId());
            }
        }
    }
}
