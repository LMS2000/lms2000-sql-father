package com.lms.sqlfather.core.generator;

import com.lms.sqlfather.core.model.enums.MockParamsRandomTypeEnum;
import com.lms.sqlfather.core.schema.TableSchema;
import com.lms.sqlfather.core.utils.FakerUtils;
import org.springframework.web.jsf.FacesContextUtils;

import javax.swing.text.html.Option;
import java.net.DatagramSocketImplFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 随机数据生成器
 */
public class RandomDataGenerator implements DataGenerator
{
    @Override
    public List<String> doGenerate(TableSchema.Field field, int rowNum) {

        String mockParams = field.getMockParams();
        List<String> result=new ArrayList<>();
        //根据模拟参数的类型faker随机生成数据
        MockParamsRandomTypeEnum mockParamsRandomTypeEnum = Optional.ofNullable(MockParamsRandomTypeEnum.getEnumByValue(mockParams))
                .orElse(MockParamsRandomTypeEnum.STRING);
        for (int i = 0; i < rowNum; i++) {
            result.add(FakerUtils.getRandomValue(mockParamsRandomTypeEnum));
        }
        return result;
    }
}
