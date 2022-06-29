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
        System.out.println(list.subList(2,8).size());
        System.out.println(list.subList(0,2).size());
    }
}
