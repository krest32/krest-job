package com.krest.job.admin.utils;

import java.util.List;
import java.util.Random;

public class LoadBalancer {

    public static String getRandomURL(List<String[]> collect) {
        int total = collect.size();
        Random random = new Random();
        return collect.get(random.nextInt(total))[0];
    }


}
