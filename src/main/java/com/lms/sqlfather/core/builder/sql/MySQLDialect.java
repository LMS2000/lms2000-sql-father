package com.lms.sqlfather.core.builder.sql;


public class MySQLDialect implements SQLDialect{

    @Override
    public String wrapFieldName(String fieldName) {
        return String.format("`%s`",fieldName);
    }

    @Override
    public String parseFieldName(String fieldName) {
        if(fieldName.startsWith("`")&&fieldName.endsWith("`")){
            return fieldName.substring(1,fieldName.length()-1);
        }
        return fieldName;
    }

    @Override
    public String wrapTableName(String tableName) {
        return String.format("`%s`",tableName);
    }

    @Override
    public String parseTableName(String tableName) {
        if(tableName.startsWith("`")&&tableName.endsWith("`")){
            return tableName.substring(1,tableName.length()-1);
        }
        return tableName;
    }
}
