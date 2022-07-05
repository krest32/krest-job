package com.krest.job.core.controller;

import com.alibaba.fastjson.JSONObject;
import com.krest.job.common.entity.KrestJobRequest;
import com.krest.job.common.entity.KrestJobResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class ClientController {

    /**
     * 检测服务是否存活
     */
    @PostMapping("detect/service")
    public String detectService(@RequestBody String requestStr) {
        KrestJobRequest krestJobRequest = JSONObject.parseObject(requestStr, KrestJobRequest.class);
        KrestJobResponse krestJobResponse = new KrestJobResponse(
                krestJobRequest.getId(),
                200, true,
                "job handler still alive",
                null, null
        );
        return JSONObject.toJSONString(krestJobResponse);
    }
}
