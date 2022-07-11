package com.krest.job.admin.cache;

import com.krest.job.common.entity.ServiceInfo;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * 本地缓存数据
 */
@ToString
public class LocalCache {

    /**
     * 监控 leader 发送心跳的超时时间
     */
    private static Long expireTime = System.currentTimeMillis();

    public static Long getExpireTime() {
        return expireTime;
    }

    public static void setExpireTime(Long expireTime) {
        LocalCache.expireTime = expireTime;
    }

    /**
     * 当前服务的类型： follower、observer、leader
     */
    private static ServiceInfo curServiceInfo;

    public static ServiceInfo getCurServiceInfo() {
        if (curServiceInfo == null) {
            synchronized (LocalCache.class) {
                curServiceInfo = new ServiceInfo();
            }
        }
        return curServiceInfo;
    }

    public static void setCurServiceInfo(ServiceInfo curServiceInfo) {
        LocalCache.curServiceInfo = curServiceInfo;
    }


    private static List<ServiceInfo> jobHandlerServiceInfos;

    public static List<ServiceInfo> getServiceInfos() {
        if (jobHandlerServiceInfos == null) {
            synchronized (LocalCache.class) {
                jobHandlerServiceInfos = new ArrayList<>();
            }
        }
        return jobHandlerServiceInfos;
    }

    public synchronized static void setServiceInfos(List<ServiceInfo> serviceInfos) {
        LocalCache.jobHandlerServiceInfos = serviceInfos;
    }


    private static List<ServiceInfo> adminServiceInfos;

    public static List<ServiceInfo> getAdminServiceInfos() {
        if (adminServiceInfos == null) {
            synchronized (LocalCache.class) {
                adminServiceInfos = new ArrayList<>();
            }
        }
        return adminServiceInfos;
    }

    public static void setAdminServiceInfos(List<ServiceInfo> adminServiceInfos) {
        LocalCache.adminServiceInfos = adminServiceInfos;
    }
}
