package com.lms.sqlfather.core.builder;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Tables;
import com.lms.contants.HttpCode;
import com.lms.sqlfather.core.generator.DataGenerator;
import com.lms.sqlfather.core.generator.DataGeneratorFactory;
import com.lms.sqlfather.core.model.enums.MockTypeEnum;
import com.lms.sqlfather.core.schema.TableSchema;
import com.lms.sqlfather.core.schema.TableSchema.Field;
import com.lms.sqlfather.exception.BusinessException;
import org.apache.commons.collections4.BagUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 数据构造器
 */
public class DataBuilder {
    public static List<Map<String, Object>> generateData(TableSchema tableSchema, int rowNum) {
        List<Field> fieldList = tableSchema.getFieldList();
        List<Map<String,Object>> dataList=new ArrayList<>(rowNum);
        //初始化map
        for (int i = 0; i < rowNum; i++) {
            dataList.add(new HashMap<>());
        }

        //将fieldList 根据是否有外键排序


        for (Field field : fieldList) {
            MockTypeEnum mockTypeEnum = Optional.ofNullable(MockTypeEnum.getEnumByValue(field.getMockType()))
                    .orElse(MockTypeEnum.NONE);
            //获取生成器
            DataGenerator dataGenerator= DataGeneratorFactory.getDataGenerator(mockTypeEnum);
            List<String> generateList = dataGenerator.doGenerate(field, rowNum);
            String fieldName = field.getFieldName();
            if(CollectionUtil.isNotEmpty(generateList)){
                for (int i = 0; i < rowNum; i++) {
                    dataList.get(i).put(fieldName,generateList.get(i));
                }
            }
        }
        return dataList;
    }

    //构造多表
  public static Map<String,List<Map<String,Object>>> generateAllData(List<TableSchema> tableSchemas){
      Map<String, TableSchema> tableSchemaMap = tableSchemas.stream().collect(Collectors.toMap(TableSchema::getTableName, Function.identity()));

      Map<String,List<Map<String,Object>>> tableDataList=new HashMap<>();

      //生成字段map
      Map<String,List<Field>> fieldMap=new HashMap<>();

      //每个表生成条数
      Map<String,Integer> rowNumMap=new HashMap<>();
      //初始化
      for (Map.Entry<String, TableSchema> stringTableSchemaEntry : tableSchemaMap.entrySet()) {
           tableDataList.put(stringTableSchemaEntry.getKey(),new ArrayList<>(stringTableSchemaEntry.getValue().getMockNum()));
           fieldMap.put(stringTableSchemaEntry.getKey(),stringTableSchemaEntry.getValue().getFieldList());
           rowNumMap.put(stringTableSchemaEntry.getKey(),stringTableSchemaEntry.getValue().getMockNum());
      }

      //记录有外键的字段
      Map<String,Field> foreignMap=new HashMap<>();

      //外键需要的数据
      Map<String,List<String>> generatorMap=new HashMap<>();

      for (Map.Entry<String, List<Field>> stringListEntry : fieldMap.entrySet()) {
          List<Field> fieldList = stringListEntry.getValue();
          String tableName = stringListEntry.getKey();
          Integer rowNum = rowNumMap.get(stringListEntry.getKey());
          //先模拟不需要外键的字段
          for (Field field : fieldList) {
              MockTypeEnum mockTypeEnum = Optional.ofNullable(MockTypeEnum.getEnumByValue(field.getMockType()))
                      .orElse(MockTypeEnum.NONE);
              if(mockTypeEnum.equals(MockTypeEnum.related)){
                  foreignMap.put(tableName,field);

                  continue;
              }
              //获取生成器
              DataGenerator dataGenerator= DataGeneratorFactory.getDataGenerator(mockTypeEnum);
              List<String> generateList = dataGenerator.doGenerate(field, rowNum);
              String fieldName = field.getFieldName();
              generatorMap.put(String.format("%s.%s",tableName,fieldName),generateList);
              if(CollectionUtil.isNotEmpty(generateList)){
                  for (int i = 0; i < rowNum; i++) {
                      tableDataList.get(tableName).get(i).put(fieldName,generateList.get(i));
                  }
              }
          }
      }
      //模拟需要外键的字段

      for (Map.Entry<String, Field> stringFieldEntry : foreignMap.entrySet()) {
          Field field = stringFieldEntry.getValue();
          String foreignKey = field.getForeignKey();
          String foreignTableName = field.getForeignTableName();
          Integer rowNum = rowNumMap.get(foreignTableName);
          List<String> generatorForeignList = generatorMap.getOrDefault(String.format("%s.%s", foreignTableName, foreignKey), null);

          BusinessException.throwIf(generatorForeignList==null, HttpCode.OPERATION_ERROR,"外键填充失败");



          //随机填充todo

          for (int i = 0; i < rowNum; i++) {
              tableDataList.get(foreignTableName).get(i).put(field.getFieldName(),generatorForeignList.get(RandomUtils.nextInt(0,generatorForeignList.size())));
          }

      }
      return tableDataList;

  }
}
