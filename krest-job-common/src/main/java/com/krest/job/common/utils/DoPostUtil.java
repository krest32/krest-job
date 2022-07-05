package com.krest.job.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.krest.job.common.entity.KrestJobMessage;
import com.krest.job.common.entity.KrestJobRequest;
import com.krest.job.common.entity.KrestJobResponse;
import com.krest.job.common.runnable.RespHandler;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;


/**
 * 发送 post 请求线程
 */
@Slf4j
public class DoPostUtil {

    /**
     * 异步发送Post请求
     */
    public static void doPost(KrestJobRequest krestJobRequest,
                              OkHttpClient okHttpClient,
                              RespHandler jobResponseHandler) {

        // Josn 格式化请求参数
        RequestBody body = RequestBody.create(JSONObject.toJSONString(krestJobRequest),
                MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(krestJobRequest.getTargetUrl())
                .post(body)
                .build();

        // 使用异步的方式发送请求
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

                KrestJobResponse krestJobResponse = new KrestJobResponse(
                        krestJobRequest.getId(),
                        400, false,
                        KrestJobMessage.CanNotConnectJobHandler, null, e);

                // 将返回的结果添加到需要处理的
                jobResponseHandler.addResponse(krestJobResponse);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                KrestJobResponse krestJobResponse;
                String resultMsg = null;
                try {
                    resultMsg = response.body().string();
                    krestJobResponse = JSONObject.parseObject(resultMsg,
                            KrestJobResponse.class);
                } catch (Exception e) {
                    krestJobResponse = new KrestJobResponse(
                            krestJobRequest.getId(),
                            200, true,
                            KrestJobMessage.CheckClientReturn, resultMsg, e);
                }
                jobResponseHandler.addResponse(krestJobResponse);
            }
        });
    }
}
