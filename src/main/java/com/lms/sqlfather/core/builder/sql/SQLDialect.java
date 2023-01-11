package com.lms.sqlfather.core.builder.sql;

public interface SQLDialect {

    /**
     *封装字段名
      * @param fieldName
     * @return
     */
    String wrapFieldName(String fieldName);

    /**
     *解析字段名
     * @param fieldName
     * @return
     */
    String parseFieldName(String fieldName);

    /**
     *封装表名
     * @param tableName
     * @return
     */
    String wrapTableName(String tableName);

    /**
     *解析表名
     * @param tableName
     * @return
     */
    String parseTableName(String tableName);
}
