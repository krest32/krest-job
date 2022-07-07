package com.krest.job.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.krest.job.common.entity.ServiceLock;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceLockMapper extends BaseMapper<ServiceLock> {

    int updateToLock(ServiceLock serviceLock);

    int updateToUnLock(ServiceLock serviceLock);
}
