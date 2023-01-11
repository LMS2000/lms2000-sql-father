package com.lms.sqlfather.core.generator;

import cn.hutool.core.date.DateUtil;
import com.lms.sqlfather.core.schema.TableSchema.Field;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 默认（不模拟）数据生成器
 */
public class DefaultDataGenerator implements DataGenerator {
    @Override
    public List<String> doGenerate(Field field, int rowNum) {
        //获取模拟参数
        String mockParams = field.getMockParams();
        List<String> result=new ArrayList<>();
        //如果是主键的话就生成递增的数据，模拟参数为空就赋值1
        if(field.isPrimaryKey()){
            if(StringUtils.isBlank(mockParams)){
                mockParams="1";
            }
            int index=Integer.parseInt(mockParams);
            for (int i = 0; i < rowNum; i++) {
                result.add(String.valueOf(index+i));
            }
       return result;
        }
        //不为主键就使用默认值生成
        String defaultValue = field.getDefaultValue();
        if("CURRENT_TIMESTAMP".equals(defaultValue)){
            defaultValue= DateUtil.format(new Date(),"yyyy-MM-dd HH:mm:ss");
        }
        if(StringUtils.isNotBlank(defaultValue)){
            for (int i = 0; i < rowNum; i++) {
                result.add(defaultValue);
            }
        }

        return result;
    }
}
