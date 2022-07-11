package com.krest.job.admin.balancer;

import com.krest.job.admin.mapper.JobHandlerMapper;
import com.krest.job.common.entity.JobHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * job handler 调用策略
 */
public class JobHandlerLoadBalancer {

    /**
     * 随机 策略
     */
    public static String randomRun(List<String[]> collect) {
        int total = collect.size();
        Random random = new Random();
        return collect.get(random.nextInt(total))[0];
    }

    /**
     * 轮询 策略
     */
    public static String roundRibbonRun(List<String[]> collect, Integer roundPos, JobHandlerMapper jobHandlerMapper, JobHandler jobHandler) {
        if (roundPos >= collect.size() - 1) {
            roundPos = 0;
        } else {
            roundPos++;
        }
        jobHandler.setAppPos(roundPos);
        jobHandlerMapper.updateById(jobHandler);
        return collect.get(roundPos)[0];
    }

    /**
     * 加权轮询
     */
    public static String weightRoundRobinRun(List<String[]> collect,
                                             Integer weightPos,
                                             JobHandlerMapper jobHandlerMapper,
                                             JobHandler jobHandler) {
        List<String> servers = new ArrayList<>();
        for (int i = 0; i < collect.size(); i++) {
            String server = collect.get(i)[0];
            int weight = Integer.valueOf(collect.get(i)[1]);
            for (int j = 0; j < weight; j++) {
                servers.add(server);
            }
        }
        if (weightPos >= servers.size() - 1) {
            weightPos = 0;
        } else {
            weightPos++;
        }
        jobHandler.setAppPos(weightPos);
        jobHandlerMapper.updateById(jobHandler);
        return servers.get(weightPos);
    }
}
