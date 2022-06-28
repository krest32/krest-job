package com.krest.job.common.utils;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    static final String dateFormat1 = "yyyy-MM-dd HH:mm:ss";

    public static String getDateFormat1() {
        return dateFormat1;
    }

    public static String getNowDate(String format) {
        Date date = new Date();
        DateFormat df = new SimpleDateFormat(format);
        String dateStr = df.format(date);
        return dateStr;
    }


    public static Date strToDate(String str, String format) {
        Date date = new Date();
        try {
            DateFormat df = new SimpleDateFormat(format);
            date = df.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String dateToString(Date date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = df.format(date);
        return dateStr;
    }


    public static Date str1ToDate(String str) {
        Date date = new Date();
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd ");
            date = df.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static Date str2ToDate(String str) {
        Date date = new Date();
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            date = df.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static Date str3ToDate(String str) {
        Date date = new Date();
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            date = df.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }


}
