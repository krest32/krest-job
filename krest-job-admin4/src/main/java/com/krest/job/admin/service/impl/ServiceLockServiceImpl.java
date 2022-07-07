package com.krest.job.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.krest.job.admin.mapper.JobHandlerMapper;
import com.krest.job.admin.mapper.ServiceLockMapper;
import com.krest.job.admin.service.ServiceLockService;
import com.krest.job.common.entity.JobHandler;
import com.krest.job.common.entity.ServiceLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ServiceLockServiceImpl extends ServiceImpl<ServiceLockMapper, ServiceLock> implements ServiceLockService {
}
