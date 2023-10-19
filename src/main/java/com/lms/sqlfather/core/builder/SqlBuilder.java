package com.lms.sqlfather.core.builder;

import com.lms.sqlfather.core.builder.sql.MySQLDialect;
import com.lms.sqlfather.core.builder.sql.SQLDialect;
import com.lms.sqlfather.core.builder.sql.SQLDialectFactory;
import com.lms.sqlfather.core.model.enums.FieldTypeEnum;
import com.lms.sqlfather.core.model.enums.MockTypeEnum;
import com.lms.sqlfather.core.schema.TableSchema;
import com.lms.sqlfather.core.schema.TableSchema.Field;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class SqlBuilder {

    private SQLDialect sqlDialect;

    public SqlBuilder() {
        this.sqlDialect = SQLDialectFactory.getSQLDialect(MySQLDialect.class.getName());
    }

    public SqlBuilder(SQLDialect sqlDialect) {
        this.sqlDialect = sqlDialect;
    }

    /**
     * 生成建表sql语句
     *
     * @param tableSchema
     * @return
     */
    public String buildCreateTableSql(TableSchema tableSchema) {
        //创建建表sql模板
        String template = "%s\n" +
                "create table if not exists %s\n" +
                "(\n" +
                "%s\n" +
                ") %s; ";
        //封装表名
        String tableName = sqlDialect.wrapTableName(tableSchema.getTableName());
        String dbName = tableSchema.getDbName();
        if (StringUtils.isNotBlank(dbName)) {
            tableName = String.format("%s.%s", dbName, tableName);
        }

        //封装表前表后注释
        String prefixComment = StringUtils.isNotBlank(tableSchema.getTableComment())? String.format("--%s", tableSchema.getTableComment()):"";

        String suffixComment =StringUtils.isNotBlank(tableSchema.getTableComment())? String.format("comment %s", tableSchema.getTableComment()):"";

        List<Field> fieldList = tableSchema.getFieldList();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < fieldList.size(); i++) {
            Field field = fieldList.get(i);
            stringBuilder.append(buildCreateFieldSql(field));
            if (i != fieldList.size() - 1) {
                stringBuilder.append(",");
                stringBuilder.append("\n");
            }
        }
        String fieldInfo = stringBuilder.toString();

        //封装建表sql模板
        String result = String.format(template, prefixComment, tableName, fieldInfo, suffixComment);

        return result;

    }

    public String buildCreateFieldSql(Field field) {

        // e.g. column_name int default 0 not null auto_increment comment '注释' primary key,
        String fieldName = sqlDialect.wrapFieldName(field.getFieldName());
        StringBuilder stringBuilder = new StringBuilder();
        String fieldType = field.getFieldType();
        String defaultValue = field.getDefaultValue();
        String comment = field.getComment();
        String onUpdate = field.getOnUpdate();
        boolean primaryKey = field.isPrimaryKey();
        boolean autoIncrement = field.isAutoIncrement();
        boolean notNull = field.isNotNull();
        //封装字段名
        stringBuilder.append(fieldName);
        //封装类型
        stringBuilder.append(" ").append(fieldType);
        //封装默认值
        if (StringUtils.isNotBlank(defaultValue)) {
            stringBuilder.append(" ").append("default ").append(getValueStr(field, defaultValue));
        }
        //封装字段是否允许为空
        stringBuilder.append(" ").append(notNull ? "not null" : "null");
        //封装字段是否自增
        if (autoIncrement) {
            stringBuilder.append(" ").append("auto_increment");
        }
        //封装字段附加
        if (StringUtils.isNotBlank(onUpdate)) {
            stringBuilder.append(" ").append("on update ").append(onUpdate);
        }
        //封装字段注释
        if (StringUtils.isNotBlank(comment)) {
            stringBuilder.append(" ").append(String.format("comment '%s'", comment));
        }
        //主键
        if (primaryKey) {
            stringBuilder.append(" ").append("primary key");
        }

        return stringBuilder.toString();

    }


    /**
     * 生成insert语句
     * @param tableSchema
     * @param dataList
     * @return
     */
    public String buildInsertSql(TableSchema tableSchema, List<Map<String, Object>> dataList) {
        String tamplate="insert into %s (%s) values (%s);";

        String dbName = tableSchema.getDbName();
        String tableName = sqlDialect.wrapTableName(tableSchema.getTableName());
        if(StringUtils.isNotBlank(tableName)){
            if(StringUtils.isNotBlank(dbName)){
                tableName=String.format("%s.%s",dbName,tableName);
            }else{
                tableName=String.format("%s",tableName);
            }

        }
        //获取字段列表
        List<Field> fieldList = tableSchema.getFieldList();

        //去除不模拟的字段
        fieldList=fieldList.stream().filter(field -> {
            MockTypeEnum mockTypeEnum = Optional.ofNullable(MockTypeEnum.getEnumByValue(field.getMockType()))
                    .orElse(MockTypeEnum.NONE);
            return !MockTypeEnum.NONE.equals(mockTypeEnum);
        }).collect(Collectors.toList());
        StringBuilder stringBuilder=new StringBuilder();
        int total=dataList.size();
        for (int i = 0; i < total; i++) {
            Map<String, Object> stringObjectMap = dataList.get(i);
            //获取字段  如srt,2131,123413,213,213,123
            String keyStr=fieldList.stream().map(field ->
                    sqlDialect.wrapFieldName(field.getFieldName()))
                    .collect(Collectors.joining(", "));
            String valueStr=fieldList.stream()
                    .map(field -> getValueStr(field,stringObjectMap.get(field.getFieldName())))
                    .collect(Collectors.joining(", "));
            String result=String.format(tamplate,tableName,keyStr,valueStr);
            stringBuilder.append(result);
            if(i!=total-1){
                stringBuilder.append("\n");
            }
        }
        return stringBuilder.toString();
    }


    public String buildInsertSql(List<TableSchema> tableSchemas, Map<String,List<Map<String,Object>>> dataMap) {
        StringBuilder stringBuilder=new StringBuilder();
        for (TableSchema tableSchema : tableSchemas) {

            List<Map<String, Object>> dataList = dataMap.get(tableSchema.getTableName());
            String insertSql = buildInsertSql(tableSchema, dataList);
            stringBuilder.append(insertSql).append("\n");
        }
        return stringBuilder.toString();
    }

    /**
     * 根据默认值获取字符串
     *
     * @param field
     * @param defaultValue
     * @return
     */
    private String getValueStr(Field field, Object defaultValue) {
        if (field == null || defaultValue == null) {
            return "''";
        }
        FieldTypeEnum fieldTypeEnum = Optional.ofNullable(FieldTypeEnum.getEnumByValue(field.getFieldType()))
                .orElse(FieldTypeEnum.TEXT);
        String result = String.valueOf(defaultValue);
        switch (fieldTypeEnum) {
            case DATETIME:
            case TIMESTAMP:
                return result.equalsIgnoreCase("CURRENT_TIMESTAMP") ? result : String.format("'%s'", defaultValue);
            case DATE:
            case TIME:
            case CHAR:
            case VARCHAR:
            case TINYTEXT:
            case TEXT:
            case MEDIUMTEXT:
            case LONGTEXT:
            case TINYBLOB:
            case BLOB:
            case MEDIUMBLOB:
            case LONGBLOB:
            case BINARY:
            case VARBINARY:
                return String.format("'%s'", defaultValue);
            default:
                return result;
        }

    }

}
