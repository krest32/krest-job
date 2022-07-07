package com.krest.job.admin.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.krest.job.admin.mapper.JobHandlerMapper;
import com.krest.job.admin.mapper.ServiceInfoMapper;
import com.krest.job.admin.schedule.SchedulerJob;
import com.krest.job.admin.schedule.SchedulerUtils;
import com.krest.job.admin.service.JobManagerService;
import com.krest.job.common.entity.JobHandler;
import com.krest.job.common.entity.KrestJobMessage;
import com.krest.job.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public R runScheduleJob(JobHandler jobHandler) {
        // 解析接口传递的Job信息报文
        if (StringUtils.isEmpty(jobHandler.getCron())) {
            String msg = "job handler info without corn!";
            log.info(msg);
            return R.ok().message(msg);
        }

        // 如果当前 Job handler 信息在数据库中不存在
        if (null == jobHandlerMapper.selectById(jobHandler.getId())) {
            return R.error().message(KrestJobMessage.JobHandleNotExist);
        }

        // 更新当前的 JobHandler 信息
        jobHandlerMapper.updateById(jobHandler);

        // 执行定时任务：job, 会以线程池的方式进行执行
        try {
            Scheduler scheduler = SchedulerUtils.CornJob(
                    jobHandler.getJobName(),
                    jobHandler.getJobGroup(),
                    jobHandler.getCron(),
                    jobHandler, SchedulerJob.class);
            scheduler.start();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return R.ok();
    }

    @Override
    public R callBack(String jobId) {
        return null;
    }


    @Override
    public R stopScheduleJob(JobHandler jobHandler) {
        jobHandler.setRunning(false);
        int cnt = jobHandlerMapper.updateById(jobHandler);
        return R.ok().data("更新任务数量", cnt);
    }
}
