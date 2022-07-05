package com.krest.job.common.entity;


public class KrestJobMessage {
    public static final String JobHandleNotExist = "job handler not exist in database";
    public static final String KrestJobFutureRunException = "krest job future run exception";
    public static final String DoesNotExistServiceForRunningJob = "does not exit service for running job";
    public static final String CanNotConnectJobHandler = "can not connect job handler";
    public static final String RegisterJobHandlerSuccess = "register job handler to krest job admin success";
    public static final String RegisterJobHandlerFailed = "register job handler to admin failed";
    public static final String ServiceIsAlive = "service is still alive.";
    public static final String DoPostRequestError = "do post request error.";
    public static final String ReTryJob = "start rerunning job : ";
    public static final String CheckClientReturn = "do post success, but get response error, please check client response";
    public static final String StopScheduleJob = "stop schedule job.";
}
