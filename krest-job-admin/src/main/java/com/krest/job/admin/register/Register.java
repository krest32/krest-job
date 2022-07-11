package com.krest.job.admin.register;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.krest.job.admin.cache.LocalCache;
import com.krest.job.admin.mapper.ServiceInfoMapper;
import com.krest.job.admin.schedule.CheckService;
import com.krest.job.common.entity.*;
import com.krest.job.common.utils.DateUtil;
import com.krest.job.common.utils.HttpUtil;
import com.krest.job.common.utils.IdWorker;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

@Component
@Slf4j
public class Register implements InitializingBean {

    final String heartBeatPath = "/service/heartbeat";

    @Autowired
    Environment environment;

    @Autowired
    ServiceInfoMapper serviceInfoMapper;

    @Autowired
    CheckService checkService;

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

        // 更新数据库的信息
        ServiceInfo serviceInfo = updateDBInfo(url);

        // 添加的本地缓存信息
        LocalCache.setCurServiceInfo(serviceInfo);


        // 同步信息
        synchInfo(serviceInfo);



        // 定时检测
        startTimer();
    }

    /**
     * 如果
     */
    private void startTimer() {
        new Timer("check leader thread").schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    detectLeader();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }, 0, 30 * 1000);
    }

    private void detectLeader() throws Throwable {
        long curMillions = System.currentTimeMillis();
        if (!LocalCache.getCurServiceInfo().getServiceRole().equals(ServiceType.LEADER)) {
            if (curMillions > LocalCache.getExpireTime()) {
                log.info("过长时间,leader 沒有发送探测报文, follower 发起反向探测");
                QueryWrapper<ServiceInfo> leaderQuery = new QueryWrapper<>();
                leaderQuery.eq("service_role", ServiceType.LEADER);
                List<ServiceInfo> leaders = serviceInfoMapper.selectList(leaderQuery);
                if (null == leaders || leaders.size() == 0) {
                    log.info("数据库中没有leader信息");
                    checkService.synchAdminService();
                }
                if (leaders.size() > 1) {
                    log.info("数据库中存在多个leader信息,开始重新同步leader");
                    checkService.synchAdminService();
                }
                if (leaders.size() == 1) {
                    log.info("开始检测leader信息 : {}", leaders.get(0).getServiceAddress());
                    ServiceInfo serviceInfo = leaders.get(0);
                    KrestJobRequest request = new KrestJobRequest(
                            UUID.randomUUID().toString(), JSONObject.toJSONString(LocalCache.getCurServiceInfo()),
                            serviceInfo.getServiceAddress() + heartBeatPath, MethodType.POST
                    );
                    KrestJobFuture jobFuture = HttpUtil.doRequest(request);
                    KrestJobResponse response = jobFuture.get();
                    if (!response.getStatus()) {
                        checkService.synchAdminService();
                    }
                }
            }
        }
    }

    /**
     * 1. 如果数据库有 leader 信息，那么就发送注册信息
     * 2. 如果数据库中没有 leader 信息，开启集群同步状态
     */
    private void synchInfo(ServiceInfo serviceInfo) throws Throwable {
        QueryWrapper<ServiceInfo> leaderQuery = new QueryWrapper<>();
        leaderQuery.eq("service_role", ServiceType.LEADER);
        List<ServiceInfo> leaderInfos = serviceInfoMapper.selectList(leaderQuery);


        if (null != leaderInfos && leaderInfos.size() > 0) {
            // 获取 leader 的远程信息, 如果注册成功，那么同时更新自己为 follower

            if (sendInfoToLeader(serviceInfo, leaderInfos)) {
                return;
            }
        }


        // 开启集群同步状态
        QueryWrapper<ServiceInfo> observerQuery = new QueryWrapper<>();
        observerQuery.ne("service_role", ServiceType.JOBHANDLER);
        List<ServiceInfo> observerInfos = serviceInfoMapper.selectList(observerQuery);

        // 如果仅有一个服务，那么说明这个服务是它自己, 自己一台服务完成注册即可
        if (null != observerInfos && observerInfos.size() == 1) {
            boolean flag = checkService.synchAdminService();
            if (flag)
                return;
            else
                log.info("初始化同步失败");
        }
    }

    private boolean sendInfoToLeader(ServiceInfo serviceInfo, List<ServiceInfo> serviceInfos) throws Throwable {
        String leaderUrl = serviceInfos.get(0).getServiceAddress() + "/service/register/follower";

        KrestJobRequest request = new KrestJobRequest();
        request.setId(UUID.randomUUID().toString());
        request.setMethodType(MethodType.POST);
        request.setTargetUrl(leaderUrl);
        request.setArgs(JSONObject.toJSONString(serviceInfo));

        KrestJobFuture jobFuture = HttpUtil.doRequest(request);
        KrestJobResponse jobResponse = jobFuture.get();

        if (jobResponse.getStatus()) {
            // 更新本地状态为 follower
            serviceInfo.setServiceRole(ServiceType.FOLLOWER);
            serviceInfoMapper.updateById(serviceInfo);
            log.info("register to leader success, leader address :{} ", leaderUrl);
            return true;
        } else {
            log.info("register to leader failed.");
            log.info("delete leader info in db : {} ", serviceInfos.get(0));
            serviceInfoMapper.deleteById(serviceInfos.get(0).getId());
            return false;
        }
    }

    @NotNull
    private ServiceInfo updateDBInfo(String url) {
        QueryWrapper<ServiceInfo> eqWrapper = new QueryWrapper<>();
        eqWrapper.eq("service_address", url);

        serviceInfoMapper.delete(eqWrapper);
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setId(IdWorker.nextId());
        serviceInfo.setServiceRole(ServiceType.OBSERVER);
        serviceInfo.setServiceAddress(url);
        serviceInfo.setCreateTime(DateUtil.getNowDate(DateUtil.getDateFormat1()));
        serviceInfo.setAppName("admin-server");
        serviceInfoMapper.insert(serviceInfo);
        return serviceInfo;
    }
}
