package com.itheima.reggie.common;


import org.springframework.aop.aspectj.annotation.MetadataAwareAspectInstanceFactory;

//自定义业务异常
public class CustomException extends RuntimeException{
    public CustomException(String message){
        super(message);
    }
}
