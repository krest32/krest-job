package com.krest.job.core.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 添加到方法的注解
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface KrestJobhandler {

}
