package com.krest.job.admin.schedule;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.krest.job.admin.cache.LocalCache;
import com.krest.job.admin.distributer.Distributer;
import com.krest.job.admin.mapper.JobHandlerMapper;
import com.krest.job.admin.mapper.ServiceInfoMapper;
import com.krest.job.common.entity.JobHandler;
import com.krest.job.common.entity.ServiceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class CheckJobs {

    @Autowired
    Distributer distributer;

    @Autowired
    ServiceInfoMapper serviceInfoMapper;

    @Autowired
    JobHandlerMapper jobHandlerMapper;

    /**
     * 自动重启运行失败的任务
     */
    @Scheduled(cron = "0/20 * * * * ?")
    public void slowCheckJob() {
        if (LocalCache.getCurServiceInfo().getServiceRole().equals(ServiceType.LEADER)) {
            QueryWrapper<JobHandler> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_running", 1);
            List<JobHandler> jobHandlers = jobHandlerMapper.selectList(queryWrapper);
            if (jobHandlers.size() > 0) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                for (JobHandler jobHandler : jobHandlers) {
                    LocalDateTime nextTime = LocalDateTime.parse(jobHandler.getNextTriggerTime(), formatter);
                    // 代表正在运行的任务已经错过了下一次的任务运行时间
                    if (nextTime.plusSeconds(10).compareTo(LocalDateTime.now()) == -1) {
                        log.info("job handler :{} , 错多了下一次运行时间 , 开始重新分配运行 service. ", jobHandler.getJobName());
                        distributer.releaseTasK(jobHandler);
                    }
                }
            }
        }
    }
}
