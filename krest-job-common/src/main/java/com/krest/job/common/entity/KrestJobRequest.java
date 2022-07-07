package com.krest.job.common.entity;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class KrestJobRequest {
    String id;
    String args;
    String targetUrl;
    MethodType methodType;

    public KrestJobRequest(String id, String url) {
        this.id = id;
        this.targetUrl = url;
    }
}
