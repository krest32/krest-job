package com.krest.job.admin.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.krest.job.admin.mapper.JobHandlerMapper;
import com.krest.job.admin.mapper.ServiceInfoMapper;
import com.krest.job.admin.service.JobManagerService;
import com.krest.job.admin.utils.LoadBalancer;
import com.krest.job.admin.utils.ShardingJob;
import com.krest.job.admin.utils.ThreadPoolConfig;
import com.krest.job.admin.utils.ThreadPoolFactory;
import com.krest.job.common.entity.JobHandler;
import com.krest.job.common.entity.JobType;
import com.krest.job.common.entity.ServiceInfo;
import com.krest.job.common.utils.HttpUtil;
import com.krest.job.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Slf4j
@Service
public class JobManagerServiceImpl implements JobManagerService {

    @Autowired
    JobHandlerMapper jobHandlerMapper;

    @Autowired
    ServiceInfoMapper serviceInfoMapper;

    @Override
    public R runJob(String jobHandlerId) {
        JobHandler jobHandler = jobHandlerMapper.selectById(jobHandlerId);
        QueryWrapper<ServiceInfo> serviceInfoQueryWrapper = new QueryWrapper<>();
        serviceInfoQueryWrapper.eq("app_name", jobHandler.getAppName());
        List<ServiceInfo> serviceInfos = serviceInfoMapper.selectList(serviceInfoQueryWrapper);
        if (serviceInfos.size() == 0) {
            return R.ok();
        }
        // 获取所有的 url 路径
        List<String[]> collect = serviceInfos.stream().map(serviceInfo -> {
            String[] params = new String[2];
            params[0] = serviceInfo.getServiceAddress() + "/" + jobHandler.getPath();
            params[1] = serviceInfo.getWeight();
            return params;
        }).collect(Collectors.toList());

        if (jobHandler.getJobType().equals(JobType.NORMAL) || collect.size() == 1) {
            return runNormalJob(jobHandler, collect);
        } else {
            return runShardingJob(jobHandler, collect);
        }

    }

    /**
     * 执行分片任务
     *
     * @param jobHandler
     * @param collect
     * @return
     */
    private R runShardingJob(JobHandler jobHandler, List<String[]> collect) {
        int total = collect.size();
        int start = 0, end = 100;
        ThreadPoolConfig poolConfig = new ThreadPoolConfig(2, 4, 10);
        ThreadPoolExecutor executor = ThreadPoolFactory.threadPoolExecutor(poolConfig);


        // 从大到小，进行排序
        collect.sort(new Comparator<String[]>() {
            @Override
            public int compare(String[] o1, String[] o2) {
                BigDecimal o1Data = new BigDecimal(o1[1]);
                BigDecimal o2Data = new BigDecimal(o2[1]);
                return o2Data.compareTo(o1Data);
            }
        });

        int curStart = start;
        int curEnd = end;
        for (int i = 0; i < collect.size(); i++) {
            curEnd = ((int) (end * Double.valueOf(collect.get(i)[1])));
            System.out.println(curStart + " " + (curStart + curEnd));
            ShardingJob shardingJob = new ShardingJob();
            shardingJob.setStart(curStart);
            shardingJob.setEnd(curStart + curEnd);
            int idx = i;
            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                boolean flag = HttpUtil.postRequest(collect.get(idx)[0], JSONObject.toJSONString(shardingJob));
                return flag;
            }, executor);
            curStart = curEnd + 1;
        }

        return R.ok();
    }

    /**
     * 执行普通任务
     *
     * @param jobHandler
     * @param collect
     * @return
     */
    private R runNormalJob(JobHandler jobHandler, List<String[]> collect) {
        boolean result;
        String clientURL = LoadBalancer.getRandomURL(collect);
        if (jobHandler.getMethodType().equals("get")) {
            result = HttpUtil.getRequest(clientURL);
        } else {
            result = HttpUtil.postRequest(clientURL, jobHandler.getArgs());
        }
        return R.ok().data("client-url", clientURL).data("result", result);
    }

    @Override
    public R callBack(String jobId) {
        return null;
    }

    @Override
    public R stop(String jobId) {
        return null;
    }

    @Override
    public R runScheduleJob(JobHandler jobHandler) {
        if (StringUtils.isEmpty(jobHandler.getCron())) {
            String msg = "job handler info without corn!";
            log.info(msg);
            return R.ok().message(msg);
        }
        CronExpression cronExpression = CronExpression.parse(jobHandler.getCron());
        LocalDateTime dateTime = cronExpression.next(LocalDateTime.now());
        jobHandler.setNextTriggerTime(dateTime.toString());
        jobHandlerMapper.updateById(jobHandler);

        System.out.println(dateTime);

        return R.ok();
    }
}
