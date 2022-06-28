package com.krest.job.admin.schedule;

import com.krest.job.common.entity.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Slf4j
public class SchedulerUtils implements InterruptableJob {

    static SchedulerFactory sf = new StdSchedulerFactory();

    //获取一个默认的Scheduler对象
    public static Scheduler getScheduler() {
        try {
            return sf.getScheduler();
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 从新开始任务
     */
    public void restartJob(String jobName, String jobGroupName) {
        Scheduler scheduler = getScheduler();
        JobKey jobKey = JobKey.jobKey(jobName, jobGroupName);
        try {
            scheduler.resumeJob(jobKey);
            log.info(String.format("从新启动job,，jobName：%s ,jobGroupName:%s", jobName, jobGroupName));
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 每小时执行一次任务
     */
    public void HoursJob(Class HoursJob) throws Exception {
        // 首先，从 SchedulerFactory 中取得一个 Scheduler 的引用
        Scheduler scheduler = sf.getScheduler();
        //jobs可以在scheduled的sched.start()方法前被调用d
        //将 job 通过 JobDetail 传给 Scheduler
        JobDetail job = newJob(HoursJob)
                .withIdentity("Hours-" + HoursJob.getName() + "-job",
                        "group-" + HoursJob.getName() + "job")
                .build();

        //设置触发方式 这里用到的是 cron 表达式
        CronTrigger trigger = newTrigger()
                .withIdentity("cron-" + HoursJob.getName() + "trigger",
                        "cron-" + HoursJob.getName() + "group")
                // 每小时执行一次
                .withSchedule(cronSchedule("0 0 * * * ? *"))
                .build();

        scheduler.scheduleJob(job, trigger);
        // 计时器开始执行
        scheduler.start();
    }


    /**
     * 根据传入的Corn表达式，指定时间去执行任务
     */
    public static Scheduler CornJob(String jobName, String jobGroupName, String cornStr,
                                    JobHandler jobHandler, Class CornJob) throws Exception {
        if (cornStr.isEmpty()) {
            throw new Exception("input hours error");
        }
        // 首先，从 SchedulerFactory 中取得一个 Scheduler 的引用
        Scheduler scheduler = getScheduler();

        //jobs可以在scheduled的sched.start()方法前被调用d
        //将 job 通过 JobDetail 传给 Scheduler
        JobDetail job = newJob(CornJob)
                .withIdentity(jobName, jobGroupName)
                .build();
        //设置触发方式 这里用到的是 cron 表达式
        CronTrigger trigger = newTrigger()
                .withIdentity(jobName, jobGroupName)
                .withSchedule(cronSchedule(cornStr))
                .build();
        // 向定时任务内传递参数
        job.getJobDataMap().put("jobHandler", jobHandler);
        scheduler.scheduleJob(job, trigger);
        // 计时器开始执行
        return scheduler;
    }


    //暂停所有任务执行
    public void standbyScheduler() {
        Scheduler scheduler = getScheduler();
        try {
            scheduler.standby();
            log.info("-----暂停任务----");
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除任务
     */
    public static void deleteJob(String jobName, String jobGroupName) {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroupName);
            Scheduler scheduler = getScheduler();
            scheduler.pauseTrigger(triggerKey);// 停止触发器
            scheduler.unscheduleJob(triggerKey);// 移除触发器
            scheduler.deleteJob(JobKey.jobKey(jobName, jobGroupName));
            log.info(String.format("删除任务，jobName：%s ,jobGroupName:%s", jobName, jobGroupName));
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }
    }

    private boolean isInterrupted = false;
    private JobKey jobKey = null;
    private int counts = 0;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        jobKey = jobExecutionContext.getJobDetail().getKey();
        log.info("【开始执行】任务Key：" + jobKey + "，执行时间： " + sdf.format(new Date()));
        try {
            for (int i = 0; i < 4; i++) {
                try {
                    Thread.sleep(1000L);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                // 查看是否中断
                if (isInterrupted) {
                    counts++;
                    log.info("被外界因素停止了这个任务key：" + jobKey + ",中断累计次数: " + counts + "\n");
                    // 也可以选择抛出一个JobExecutionException，根据业务需要指定行为
                    return;
                }
            }
        } finally {
            log.info("【完成任务】key：" + jobKey + " 完成时间：" + sdf.format(new Date()));
        }
    }


    @Override
    public void interrupt() throws UnableToInterruptJobException {
        log.info("\n—————— 【中断】外界正在调用调度器停止这个任务key：" + jobKey + " ————————");
        isInterrupted = true;
    }

}
