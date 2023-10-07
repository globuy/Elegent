package com.itheima.reggie.common;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * 基于ThreadLocal封装工具类
 */
public class BaseContext {
    public static ThreadLocal<Long> threadLocal = new ThreadLocal<Long>();

    public static void setCurrentId(long id){
        threadLocal.set(id);
    }

    public static long getCurrentId(){
        return threadLocal.get();
    }

}
