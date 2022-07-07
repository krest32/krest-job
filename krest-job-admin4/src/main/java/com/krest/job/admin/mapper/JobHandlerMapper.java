package com.krest.job.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.krest.job.common.entity.JobHandler;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface JobHandlerMapper extends BaseMapper<JobHandler> {

    List<JobHandler> selectNotRunningJobHandler();
}
