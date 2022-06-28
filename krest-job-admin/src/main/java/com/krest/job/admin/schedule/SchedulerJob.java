package com.krest.job.admin.schedule;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.krest.job.admin.balancer.LoadBalancer;
import com.krest.job.admin.mapper.JobHandlerMapper;
import com.krest.job.admin.mapper.JobLogMapper;
import com.krest.job.admin.mapper.ServiceInfoMapper;
import com.krest.job.admin.utils.ThreadPoolConfig;
import com.krest.job.admin.utils.ThreadPoolFactory;
import com.krest.job.common.balancer.LoadBalancerType;
import com.krest.job.common.entity.*;
import com.krest.job.common.utils.DateUtils;
import com.krest.job.common.utils.HttpUtil;
import com.krest.job.common.utils.IdWorker;
import com.krest.job.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Slf4j
@Component
public class SchedulerJob implements Job {

    JobHandler jobHandler = null;

    IdWorker idWorker = new IdWorker();

    @Autowired
    JobHandlerMapper jobHandlerMapper;

    @Autowired
    ServiceInfoMapper serviceInfoMapper;

    @Autowired
    JobLogMapper jobLogMapper;

    //定义一个静态时实例
    public static SchedulerJob schedulerJob;

    //使用这个java注解，让静态实例联系到mapper接口，下边这个方法完全写上，修改为自己的东西
    @PostConstruct
    public void init() {
        schedulerJob = this;
        schedulerJob.serviceInfoMapper = serviceInfoMapper;
        schedulerJob.jobHandlerMapper = jobHandlerMapper;
        schedulerJob.jobLogMapper = jobLogMapper;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        this.jobHandler = (JobHandler) jobExecutionContext.getJobDetail().getJobDataMap().get("jobHandler");
        runJob();
    }

    public void runJob() {
        if (this.jobHandler == null) {
            log.error("jobHandler 为 null");
        } else {
            this.jobHandler = schedulerJob.jobHandlerMapper.selectById(this.jobHandler.getId());
            if (this.jobHandler.isRunning()) {
                QueryWrapper<ServiceInfo> serviceInfoQueryWrapper = new QueryWrapper<>();
                serviceInfoQueryWrapper.eq("app_name", this.jobHandler.getAppName());
                List<ServiceInfo> serviceInfos = schedulerJob.serviceInfoMapper.selectList(serviceInfoQueryWrapper);
                if (serviceInfos.size() == 0) {
                    log.info("不存在可运行任务的服务");
                } else {
                    // 获取所有的 url 路径
                    List<String[]> collect = serviceInfos.stream().map(serviceInfo -> {
                        String[] params = new String[2];
                        params[0] = serviceInfo.getServiceAddress() + "/" + this.jobHandler.getPath();
                        params[1] = serviceInfo.getWeight();
                        return params;
                    }).collect(Collectors.toList());
                    if (this.jobHandler.getJobType().equals(JobType.NORMAL)) {
                        // 开始执行定时任务
                        runNormalJob(this.jobHandler, collect);
                    } else if (this.jobHandler.getJobType().equals(JobType.SHARDING)) {
                        // 执行分片任务
                        runShardingJob(this.jobHandler, collect);
                    }
                }
            } else {
                stopRunJob(this.jobHandler);
            }
        }
    }

    /**
     * 执行分片任务
     */
    private void runShardingJob(JobHandler jobHandler, List<String[]> collect) {
        String params = jobHandler.getArgs();
        List<String> list = (List<String>) JSONObject.parse(params);
        List<ShardingJob> shardingJobs = new ArrayList<>();
        int totalServer = collect.size();
        int totalWeight = 0;
        // 生成每个请求的请求参数
        for (int i = 0; i < totalServer; i++) {
            ShardingJob shardingJob = new ShardingJob();
            shardingJob.setTotalSharding(totalServer);
            shardingJob.setShardingId(i);
            int weight = Integer.valueOf(collect.get(i)[1]);
            shardingJob.setWeight(weight);
            shardingJobs.add(shardingJob);
            totalWeight += weight;
        }

        // 为每个 ShardingJob 分配数据
        ThreadPoolConfig poolConfig = new ThreadPoolConfig(5, 8, 10);
        ThreadPoolExecutor executor = ThreadPoolFactory.threadPoolExecutor(poolConfig);

        int start = 0;
        int idx = 0;
        int totalDataSize = list.size();
        for (ShardingJob shardingJob : shardingJobs) {
            List<String> tempList = new ArrayList<>();
            int end = start + shardingJob.getWeight() * totalDataSize / totalWeight;
            tempList.addAll(list.subList(start, end));
            start += end;
            shardingJob.setData(tempList);
            String requestJsonStr = JSONObject.toJSONString(shardingJob);
            // 开启并执行一个异步任务，可以指定我们的线程池
            int finalIdx = idx;
            CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> {
                return HttpUtil.postRequest(collect.get(finalIdx)[0], requestJsonStr);
            }, executor);
            idx++;
        }
    }

    /**
     * 停止执行任务
     */
    private void stopRunJob(JobHandler jobHandler) {
        SchedulerUtils.deleteJob(jobHandler.getJobName(), jobHandler.getJobGroup());
    }

    /**
     * 执行普通任务
     */
    private void runNormalJob(JobHandler jobHandler, List<String[]> collect) {
        LoadBalancerType loadBalancerType = jobHandler.getLoadBalanceType();
        String clientUrl;
        // 获取已经执行的 Url 标志
        int pos = getPos(jobHandler);
        switch (loadBalancerType) {
            case ROUNDRIBBON:
                clientUrl = LoadBalancer.roundRibbonRun(collect, pos, schedulerJob.jobHandlerMapper, jobHandler);
                break;

            case WEIGHTROUNDRIBBON:
                clientUrl = LoadBalancer.weightRoundRobinRun(collect, pos, schedulerJob.jobHandlerMapper, jobHandler);
                break;

            default:
                clientUrl = LoadBalancer.randomRun(collect);
                break;
        }
        String result = null;
        int retryCnt = 0;
        while (retryCnt < 3) {
            // 开始执行任务
            if (jobHandler.getMethodType().equals("get")) {
                result = HttpUtil.getRequest(clientUrl);
            } else {
                KrestJobRequest krestJobRequest = new KrestJobRequest();
                krestJobRequest.setMsg(jobHandler.getArgs());
                result = HttpUtil.postRequest(clientUrl, JSONObject.toJSONString(krestJobRequest));
            }
            if (!StringUtils.isEmpty(result)) {
                break;
            } else {
                retryCnt++;
            }
        }

        // 记录日志
        JobLog jobLog = new JobLog();
        jobLog.setLogId(idWorker.nextId());
        jobLog.setJobId(jobHandler.getId());
        jobLog.setRunApp(clientUrl);
        jobLog.setResultMsg(result);
        jobLog.setRetryCount(retryCnt);
        jobLog.setCreateTime(DateUtils.getNowDate(DateUtils.getDateFormat1()));
        schedulerJob.jobLogMapper.insert(jobLog);

        log.info(Thread.currentThread().getName() + "任务执行结果:{}", result);
    }

    private int getPos(JobHandler jobHandler) {
        return schedulerJob.jobHandlerMapper.selectById(jobHandler.getId()).getAppPos();
    }
}
