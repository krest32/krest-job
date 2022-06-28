package com.krest.job.common.utils;

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

    public static boolean postRequest(String targetUrl, String requestBody) {
        boolean flag = true;
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
        } catch (IOException e) {
            flag = false;
            log.error(e.getMessage(), e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return flag;
    }

    public static boolean getRequest(String targetUrl) {
        boolean flag = true;
        Request request = new Request.Builder()
                .url(targetUrl)
                .get()
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        Call call = okHttpClient.newCall(request);
        Response response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
            flag = false;
            log.error(e.getMessage(), e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return flag;
    }
}
