package com.krest.job.admin.register;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.krest.job.admin.cache.LocalCache;
import com.krest.job.admin.config.AdminProperties;
import com.krest.job.admin.mapper.ServiceInfoMapper;
import com.krest.job.common.entity.*;
import com.krest.job.common.utils.DateUtil;
import com.krest.job.common.utils.HttpUtil;
import com.krest.job.common.utils.IdWorker;
import com.krest.job.common.utils.JobIdGenerator;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.List;

@Component
@Slf4j
public class Register implements InitializingBean {


    @Autowired
    Environment environment;

    @Autowired
    AdminProperties adminProperties;

    @Autowired
    ServiceInfoMapper serviceInfoMapper;

    @SneakyThrows
    @Override
    public void afterPropertiesSet() {
        init();
    }

    private void init() throws Throwable {
        // 向数据库中注册自己
        String port = environment.getProperty("server.port");
        InetAddress localHost = Inet4Address.getLocalHost();
        String ip = localHost.getHostAddress();
        String url = "http://" + ip + ":" + port;

        // 如果数据库中有自己的信息，就删除
        QueryWrapper<ServiceInfo> eqWrapper = new QueryWrapper<>();
        eqWrapper.eq("service_address", url);
        serviceInfoMapper.delete(eqWrapper);
        // 重新加入自己的信息到数据库
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setId(IdWorker.nextId());
        serviceInfo.setServiceRole(ServiceType.OBSERVER);
        serviceInfo.setServiceAddress(url);
        serviceInfo.setCreateTime(DateUtil.getNowDate(DateUtil.getDateFormat1()));
        serviceInfo.setAppName("admin-server");
        serviceInfoMapper.insert(serviceInfo);

        // 添加自己的本地缓存信息
        LocalCache.setCurServiceInfo(serviceInfo);

        // 向leader注册自己
        QueryWrapper<ServiceInfo> leaderQuery = new QueryWrapper<>();
        leaderQuery.eq("service_role", ServiceType.LEADER);
        List<ServiceInfo> serviceInfos = serviceInfoMapper.selectList(leaderQuery);
        System.out.println(1);
        if (null != serviceInfos && serviceInfos.size() > 0) {
            System.out.println(2);
            if (serviceInfos.get(0).getServiceAddress() != url) {
                System.out.println(3);

                String leaderUrl = serviceInfos.get(0).getServiceAddress() + "/service/register/follower";
                KrestJobRequest krestJobRequest = new KrestJobRequest();
                krestJobRequest.setId(JobIdGenerator.getNextJobId());
                krestJobRequest.setMethodType(MethodType.POST);
                krestJobRequest.setTargetUrl(leaderUrl);
                krestJobRequest.setArgs(JSONObject.toJSONString(serviceInfo));
                KrestJobFuture jobFuture = HttpUtil.doRequest(krestJobRequest);
                KrestJobResponse jobResponse = jobFuture.get();
                if (jobResponse.getStatus()) {
                    log.info("register to leader success, leader address :{} ", leaderUrl);
                } else {
                    log.info("register to leader failed, leader address :{} ", leaderUrl);
                }
            } else {
                System.out.println(4);
                log.info("本机就是 leader");
            }
            System.out.println(5);
        }

        // 更新本地的 adminServiceInfo list
        QueryWrapper<ServiceInfo> adminQuery = new QueryWrapper<>();
        adminQuery.ne("service_role", ServiceType.JOBHANDLER);
        LocalCache.setAdminServiceInfos(serviceInfoMapper.selectList(null));
    }
}
