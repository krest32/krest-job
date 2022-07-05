package com.krest.job.common.entity;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class KrestJobRequest {
    int id;
    String args;
    String targetUrl;
    MethodType methodType;


    public KrestJobRequest(int id, String url) {
        this.id = id;
        this.targetUrl = url;
    }
}
