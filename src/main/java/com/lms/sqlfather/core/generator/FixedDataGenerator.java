package com.lms.sqlfather.core.generator;

import com.lms.sqlfather.core.schema.TableSchema.Field;
import org.apache.commons.lang3.StringUtils;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * 固定值数据生成器
 */
public class FixedDataGenerator implements DataGenerator {

    @Override
    public List<String> doGenerate(Field field, int rowNum) {
        String mockParams = field.getMockParams();
        List<String> result=new ArrayList<>();
        if(StringUtils.isBlank(mockParams)){
            mockParams="6";
        }
        for (int i = 0; i < rowNum; i++) {
            result.add(mockParams);
        }

        return result;
    }
}
