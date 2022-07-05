package com.krest.job.admin.schedule;

import com.alibaba.fastjson.JSONObject;
import com.krest.job.admin.balancer.LoadBalancer;
import com.krest.job.admin.cache.LocalCache;
import com.krest.job.admin.mapper.JobHandlerMapper;
import com.krest.job.admin.mapper.JobLogMapper;
import com.krest.job.admin.mapper.ServiceInfoMapper;
import com.krest.job.common.balancer.LoadBalancerType;
import com.krest.job.common.entity.*;
import com.krest.job.common.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Slf4j
@Component
public class SchedulerJob implements Job {

    JobHandler jobHandler;

    @Autowired
    CheckService checkService;

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
        schedulerJob.checkService = checkService;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        this.jobHandler = (JobHandler) jobExecutionContext.getJobDetail().getJobDataMap().get("jobHandler");
        runJob();
    }

    /**
     * 开始执行定时任务
     */
    public void runJob() {
        if (this.jobHandler == null) {
            log.error("jobHandler 为 null");
        } else {
            this.jobHandler = schedulerJob.jobHandlerMapper.selectById(this.jobHandler.getId());
            if (this.jobHandler.isRunning()) {


                long start = System.currentTimeMillis();
                // 开始尝试运行任务
                List<JobLog> jobLogs = tryRunJob();
                // 解析日志
                for (JobLog jobLog : jobLogs) {
                    schedulerJob.jobLogMapper.insert(jobLog);
                }

                long end = System.currentTimeMillis();
                System.out.println((end - start) * 0.001 + " s");

            } else {
                stopRunJob(this.jobHandler);
            }
        }
    }

    private List<JobLog> tryRunJob() {
        int tryCnt = 0;
        List<JobLog> jobLogs = new ArrayList<>();
        while (tryCnt < this.jobHandler.getRetryTimes()) {

            List<ServiceInfo> serviceInfos = LocalCache.getServiceInfos().stream().filter(
                    serviceInfo ->
                            serviceInfo.getAppName().equals(this.jobHandler.getAppName()))
                    .collect(Collectors.toList());


            // 开始执行任务
            if (runJod(jobLogs, serviceInfos)) break;
            // 如果需要重试，则要更新 service 信息
            tryCnt++;
            schedulerJob.checkService.detectService();
            log.info(KrestJobMessage.ReTryJob + " " + tryCnt);
        }

        // 超过重试次数
        if (tryCnt >= this.jobHandler.getRetryTimes()) {
            log.info(KrestJobMessage.DoesNotExistServiceForRunningJob);
            JobLog jobLog = new JobLog();
            jobLog.setLogId(IdWorker.nextId());
            jobLog.setCreateTime(DateUtil.getNowDate(DateUtil.getDateFormat1()));
            jobLog.setJobId(this.jobHandler.getId());
            jobLog.setRequestArgs(this.jobHandler.getArgs());
            jobLog.setRunApp(null);
            jobLog.setExceptionMsg(KrestJobMessage.DoesNotExistServiceForRunningJob);

            // 清空，然后重新添加
            jobLogs.clear();
            jobLogs.add(jobLog);
            this.jobHandler.setRunning(false);
            stopRunJob(this.jobHandler);
        }

        for (JobLog jobLog : jobLogs) {
            jobLog.setRetryCount(tryCnt);
        }

        return jobLogs;
    }

    private boolean runJod(List<JobLog> jobLogs, List<ServiceInfo> serviceInfos) {

        List<String[]> serviceData = serviceInfos.stream().map(serviceInfo -> {
            String[] params = new String[2];
            params[0] = serviceInfo.getServiceAddress() + "/" + this.jobHandler.getPath();
            params[1] = serviceInfo.getWeight();
            return params;
        }).collect(Collectors.toList());

        if (null == serviceData || serviceData.size() == 0)
            return false;


        if (this.jobHandler.getJobType().equals(JobType.NORMAL)) {
            // 开始执行定时任务
            if (runNormalJob(serviceData, jobLogs))
                return true;

        } else if (this.jobHandler.getJobType().equals(JobType.SHARDING)) {
            // 执行分片任务
            if (runShardingJob(serviceData, jobLogs))
                return true;
        } else {
            log.info("unknown job type");

        }
        return false;
    }

    /**
     * 执行分片任务
     */
    private Boolean runShardingJob(List<String[]> collect, List<JobLog> jobLogs) {
        jobLogs.clear();
        String params = jobHandler.getArgs();
        String batchId = IdWorker.nextId();
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

        int start = 0;
        int idx = 0;
        int totalDataSize = list.size();
        List<KrestJobFuture> jobFutures = new ArrayList<>();
        for (ShardingJob shardingJob : shardingJobs) {
            JobLog jobLog = new JobLog();
            jobLog.setBatchId(batchId);
            jobLog.setLogId(IdWorker.nextId());
            jobLog.setJobId(this.jobHandler.getId());
            jobLog.setCreateTime(DateUtil.getNowDate(DateUtil.getDateFormat1()));

            List<String> tempList = new ArrayList<>();
            int end = start + shardingJob.getWeight() * totalDataSize / totalWeight;
            // 如果是最后一个服务，那么就将所有的
            if (idx == collect.size() - 1) {
                tempList.addAll(list.subList(start, list.size()));
            } else {
                tempList.addAll(list.subList(start, end));
            }
            start += end;
            shardingJob.setData(tempList);
            String clientUrl = collect.get(idx)[0];


            KrestJobRequest krestJobRequest = new KrestJobRequest(JobIdGenerator.getNextJobId(), clientUrl);
            krestJobRequest.setArgs(JSONObject.toJSONString(shardingJob));
            krestJobRequest.setMethodType(this.jobHandler.getMethodType());

            jobLog.setRunApp(clientUrl);
            jobLog.setRequestArgs(JSONObject.toJSONString(shardingJob.getData()));

            KrestJobFuture jobFuture = HttpUtil.doRequest(krestJobRequest);
            idx++;
            jobFutures.add(jobFuture);
            jobLogs.add(jobLog);
        }

        if (AnalyseUtil.anaylise(jobFutures, jobLogs)) {
            return true;
        }
        return false;

    }


    /**
     * 执行普通任务
     */
    private boolean runNormalJob(List<String[]> collect, List<JobLog> jobLogs) {
        JobLog jobLog;
        if (jobLogs.size() == 0) {
            jobLog = new JobLog();
        } else {
            jobLog = jobLogs.get(0);
        }
        jobLog.setJobId(this.jobHandler.getId());
        jobLog.setLogId(IdWorker.nextId());
        jobLog.setBatchId(IdWorker.nextId());
        jobLog.setRequestArgs(this.jobHandler.getArgs());
        jobLog.setCreateTime(DateUtil.getNowDate(DateUtil.getDateFormat1()));

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

        jobLog.setRunApp(clientUrl);

        KrestJobFuture jobFuture;
        KrestJobRequest krestJobRequest = new KrestJobRequest(JobIdGenerator.getNextJobId(), clientUrl);
        krestJobRequest.setMethodType(jobHandler.getMethodType());
        jobFuture = HttpUtil.doRequest(krestJobRequest);
        List<KrestJobFuture> jobFutures = new ArrayList<>();


        jobFutures.add(jobFuture);
        jobLogs.add(jobLog);

        if (AnalyseUtil.anaylise(jobFutures, jobLogs)) {
            return true;
        }
        return false;
    }

    private int getPos(JobHandler jobHandler) {
        return schedulerJob.jobHandlerMapper.selectById(jobHandler.getId()).getAppPos();
    }

    private void stopRunJob(JobHandler jobHandler) {
        SchedulerUtils.deleteJob(jobHandler.getJobName(), jobHandler.getJobGroup());
    }
}
