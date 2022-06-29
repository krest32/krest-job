package com.krest.job.common.utils;

import com.krest.job.common.entity.KrestJobRequest;
import com.krest.job.common.entity.KrestJobResponse;
import com.krest.job.common.entity.ShardingJob;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;

@Slf4j
public class HttpUtil {

    public static volatile OkHttpClient okHttpClient;

    private HttpUtil() {
    }

    public synchronized static OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            synchronized (HttpUtil.class) {
                if (okHttpClient == null) {
                    okHttpClient = new OkHttpClient();
                }
            }
        }
        return okHttpClient;
    }

    public static KrestJobResponse postRequest(String targetUrl, String requestBody) {
        KrestJobResponse krestJobResponse = new KrestJobResponse();
        String result = null;
        RequestBody body = RequestBody.create(requestBody, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(targetUrl)
                .post(body)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        Call call = okHttpClient.newCall(request);
        Response response = null;
        try {
            response = call.execute();
            result = response.body().string();
            krestJobResponse.setStatus(true);
            krestJobResponse.setCode(200);
            krestJobResponse.setMsg(result);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            krestJobResponse.setMsg(e.getMessage());
            krestJobResponse.setStatus(false);
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return krestJobResponse;
    }

    public static KrestJobResponse getRequest(String targetUrl) {
        KrestJobResponse result = new KrestJobResponse();
        Request request = new Request.Builder()
                .url(targetUrl)
                .get()
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        Call call = okHttpClient.newCall(request);
        Response response = null;
        try {
            response = call.execute();
            result.setCode(200);
            result.setStatus(true);
            result.setMsg(response.body().string());
        } catch (IOException e) {
            result.setStatus(false);
            result.setCode(201);
            log.error(e.getMessage(), e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return result;
    }
}
