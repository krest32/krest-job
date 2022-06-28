package com.krest.demo;

import com.alibaba.fastjson.JSONObject;
import com.krest.job.common.entity.ShardingJob;

import java.util.ArrayList;
import java.util.List;

public class test {

    public static void main(String[] args) throws Exception {
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        list.add("5");
        list.add("6");
        list.add("7");
        list.add("8");
        list.add("9");
        ShardingJob shardingJob = new ShardingJob();
        shardingJob.setData(list);
        String s = JSONObject.toJSONString(shardingJob);
        System.out.println(s);
        String data = "{\"data\":[\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\"]}";
    }

}
