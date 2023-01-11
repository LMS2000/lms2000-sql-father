package com.lms.sqlfather.core.builder;

import cn.hutool.core.collection.CollectionUtil;
import com.lms.sqlfather.core.generator.DataGenerator;
import com.lms.sqlfather.core.generator.DataGeneratorFactory;
import com.lms.sqlfather.core.model.enums.MockTypeEnum;
import com.lms.sqlfather.core.schema.TableSchema;
import com.lms.sqlfather.core.schema.TableSchema.Field;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;

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
}
