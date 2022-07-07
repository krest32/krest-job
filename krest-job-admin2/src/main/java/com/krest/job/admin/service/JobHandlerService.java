package com.krest.job.admin.service;


import com.krest.job.common.entity.JobHandler;
import com.krest.job.common.utils.R;

/**
 * @author Administrator
 */
public interface JobHandlerService {

    R registryJobHandler(JobHandler jobHandler);

    R searchJobHandlerById(String jobHandlerId);
}
