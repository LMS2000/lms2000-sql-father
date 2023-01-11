package com.lms.sqlfather.core.builder.sql;

import com.lms.sqlfather.common.ErrorCode;
import com.lms.sqlfather.exception.BusinessException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SQLDialectFactory {
    private static final Map<String, SQLDialect> DIALECT_POOL = new ConcurrentHashMap<>();

    public SQLDialectFactory() {
    }


    public static SQLDialect getSQLDialect(String className) {
        SQLDialect sqlDialect = DIALECT_POOL.get(className);
        if (null == sqlDialect) {
            ////双重检测加同步锁
            //public class LazyMan {
            //    private static LazyMan lazyMan;
            //    public LazyMan(){
            //        System.out.println(Thread.currentThread().getName()+"ok");
            //    }
            //    public static  LazyMan getInstance(){
            //        if(lazyMan==null){
            //            synchronized (LazyMan.class){
            //                if(lazyMan==null){
            //                    lazyMan=new LazyMan();
            //                }
            //            }
            //        }
            //        return lazyMan;
            //    }
            //}
            //使用同步块是因为在高并发的情况下保证单例
            synchronized (className.intern()) {
                //computeIfAbsent 方法 ：如果这个key存在就对这个value进行操作，如果不存在就创建，然后进行操作
                sqlDialect = DIALECT_POOL.computeIfAbsent(className,
                        key -> {
                            try {
                                return (SQLDialect) Class.forName(className).newInstance();
                            } catch (Exception ex) {
                                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                            }
                        });
            }
        }
        return sqlDialect;
    }
}
