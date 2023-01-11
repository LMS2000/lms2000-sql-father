package com.lms.sqlfather.core.generator;

import com.lms.sqlfather.core.schema.TableSchema.Field;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 递增数据生成器
 */
public class IncreaseDataGenerator implements DataGenerator {
    @Override
    public List<String> doGenerate(Field field, int rowNum) {
        String mockParams = field.getMockParams();
        List<String> result=new ArrayList<>();
        if(StringUtils.isBlank(mockParams)){
            mockParams="1";
        }
        int index=Integer.parseInt(mockParams);
        for (int i = 0; i < rowNum; i++) {
            result.add(String.valueOf(index+i));
        }
        return result;
    }
}
