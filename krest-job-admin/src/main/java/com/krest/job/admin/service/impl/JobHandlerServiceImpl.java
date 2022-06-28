package com.krest.job.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.krest.job.admin.mapper.JobHandlerMapper;
import com.krest.job.admin.service.JobHandlerService;
import com.krest.job.common.entity.JobHandler;
import com.krest.job.common.utils.R;
import org.springframework.stereotype.Service;

@Service
public class JobHandlerServiceImpl extends ServiceImpl<JobHandlerMapper, JobHandler> implements JobHandlerService {


    @Override
    public R registryJobHandler(JobHandler jobHandler) {
        QueryWrapper<JobHandler> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_name", jobHandler.getAppName());
        queryWrapper.eq("path", jobHandler.getPath());
        queryWrapper.eq("method_type", jobHandler.getMethodType());
        Integer count = baseMapper.selectCount(queryWrapper);
        if (count == 0) {
            baseMapper.insert(jobHandler);
        }
        return R.ok();
    }

    @Override
    public R searchJobHandlerById(String jobHandlerId) {
        JobHandler jobHandler = baseMapper.selectById(jobHandlerId);
        return R.ok().data("data", jobHandler);
    }


}
