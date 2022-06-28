package com.krest.job.core.annotation;

import com.krest.job.common.entity.JobType;
import com.krest.job.common.entity.MethodType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Administrator
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface KrestJobExecutor {
    String path();

    MethodType method();

    JobType jobType() default JobType.NORMAL;
}
