package com.lms.sqlfather.core.builder.sql;


import com.lms.contants.HttpCode;
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

            //使用同步块是因为在高并发的情况下保证单例
            synchronized (className.intern()) {
                //computeIfAbsent 方法 ：如果这个key存在就对这个value进行操作，如果不存在就创建，然后进行操作
                sqlDialect = DIALECT_POOL.computeIfAbsent(className,
                        key -> {
                            try {
                                return (SQLDialect) Class.forName(className).newInstance();
                            } catch (Exception ex) {
                                throw new BusinessException(HttpCode.SYSTEM_ERROR);
                            }
                        });
            }
        }
        return sqlDialect;
    }
}
