package com.krest.job.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.krest.job.common.entity.KrestJobFuture;
import com.krest.job.common.entity.KrestJobRequest;
import com.krest.job.common.runnable.RespHandler;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;


@Slf4j
public class HttpUtil {


    static OkHttpClient okHttpClient = new OkHttpClient();
    static RespHandler jobResponseHandler = new RespHandler(4);

    private static KrestJobFuture postRequest(KrestJobRequest krestJobRequest) {

        KrestJobFuture krestJobFuture = new KrestJobFuture(
                krestJobRequest.getId(), JSONObject.toJSONString(krestJobRequest), 0
        );
        DoPostUtil.doPost(krestJobRequest, okHttpClient, jobResponseHandler);
        jobResponseHandler.register(krestJobRequest.getId(), krestJobFuture);
        return krestJobFuture;

    }

    /**
     * get 请求不能发送请求参数
     */


    public static KrestJobFuture doRequest(KrestJobRequest krestJobRequest) {
        KrestJobFuture krestJobFuture;
        switch (krestJobRequest.getMethodType()) {
            default:
                krestJobFuture = postRequest(krestJobRequest);
                break;
        }
        return krestJobFuture;
    }
}
