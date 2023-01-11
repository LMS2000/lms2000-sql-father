package com.lms.sqlfather.core.generator;

import com.lms.sqlfather.core.schema.TableSchema;
import com.mifmif.common.regex.Generex;

import java.net.DatagramSocketImplFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * 规则数据生成器
 */
public class RuleDataGenerator implements DataGenerator {

    @Override
    public List<String> doGenerate(TableSchema.Field field, int rowNum) {
        String mockParams = field.getMockParams();
        List<String> result=new ArrayList<>();
        //根据正则表达式随机生成
        Generex generex=new Generex(mockParams);
        for (int i = 0; i < rowNum; i++) {
            result.add(generex.random());
        }
        return result;
    }
}
