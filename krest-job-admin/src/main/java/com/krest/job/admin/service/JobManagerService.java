package com.krest.job.admin.service;


import com.krest.job.common.entity.JobHandler;
import com.krest.job.common.utils.R;

public interface JobManagerService {

    R runJob(String jobHandlerId);

    R callBack(String jobHandlerId);

    R stopScheduleJob(JobHandler jobHandler);

    R runScheduleJob(JobHandler jobHandler);
}
