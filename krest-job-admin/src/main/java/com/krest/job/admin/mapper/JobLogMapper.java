package com.krest.job.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.krest.job.common.entity.JobHandler;
import com.krest.job.common.entity.JobLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JobLogMapper extends BaseMapper<JobLog> {

}
