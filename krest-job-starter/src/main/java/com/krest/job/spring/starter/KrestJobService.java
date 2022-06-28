package com.krest.job.spring.starter;

import com.alibaba.fastjson.JSONObject;
import com.krest.job.common.entity.ServiceInfo;
import com.krest.job.common.utils.DateUtils;
import com.krest.job.common.utils.IdWorker;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;

@Slf4j
public class KrestJobService {

    private KrestJobConfig krestJobConfig;

    public KrestJobService(KrestJobConfig krestJobConfig) {
        this.krestJobConfig = krestJobConfig;
    }

    public String sayHello() {
        return "ok";
    }

    /**
     * 开始注册服务
     */
    public boolean registerService() {
        Response response = null;
        try {
            // 获取服务配置的基本信息
            String dateFormat = "yyyy-MM-dd HH:mm:ss";
            ServiceInfo serviceInfo = new ServiceInfo();
            IdWorker idWorker = new IdWorker();
            serviceInfo.setId(idWorker.nextId());
            serviceInfo.setWeight(krestJobConfig.getWeight());
            serviceInfo.setServiceAddress(krestJobConfig.getClient_address());
            serviceInfo.setAppName(krestJobConfig.getClient_app_name());
            serviceInfo.setCreateTime(DateUtils.getNowDate(dateFormat));

            // 开始注册服务
            String adminUrl = krestJobConfig.getAdmin_address() + "/service/register";
            String requestBodyJson = JSONObject.toJSONString(serviceInfo);
            RequestBody body = RequestBody.create(requestBodyJson, MediaType.parse("application/json"));

            Request request = new Request.Builder()
                    .url(adminUrl)
                    .post(body)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);
            response = call.execute();

            return true;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return false;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
}
