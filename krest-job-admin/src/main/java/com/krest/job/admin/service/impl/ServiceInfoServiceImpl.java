package com.krest.job.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.krest.job.admin.mapper.ServiceInfoMapper;
import com.krest.job.admin.service.ServiceInfoService;
import com.krest.job.common.entity.ServiceInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ServiceInfoServiceImpl extends ServiceImpl<ServiceInfoMapper, ServiceInfo> implements ServiceInfoService {


    @Override
    public synchronized boolean saveServiceInfo(ServiceInfo serviceInfo) {
        // 判断当前服务是否存在
        QueryWrapper<ServiceInfo> queryWrapper = new QueryWrapper();
        queryWrapper.eq("service_address", serviceInfo.getServiceAddress());
        Integer cnt = baseMapper.selectCount(queryWrapper);
        if (cnt > 0) {
            log.info("服务：{} ,已经注册，无需重复注册", serviceInfo.getServiceAddress());
            return true;
        } else {
            // 如果服务尚未注册，那么就保存到数据库
            int insert = baseMapper.insert(serviceInfo);
            log.info("服务：{} ,注册成功", serviceInfo.getServiceAddress());
            return insert > 0;
        }
    }

    @Override
    public ServiceInfo getService(String id) {
        return baseMapper.selectById(id);
    }

    @Override
    public List<ServiceInfo> getSetviceList(String serviceName) {
        QueryWrapper<ServiceInfo> queryWrapper = new QueryWrapper();
        queryWrapper.eq("app_name", serviceName);
        List<ServiceInfo> serviceInfos = baseMapper.selectList(queryWrapper);
        return serviceInfos;
    }
}
