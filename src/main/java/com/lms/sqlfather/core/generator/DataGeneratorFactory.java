package com.lms.sqlfather.core.generator;

import com.lms.sqlfather.core.model.enums.MockTypeEnum;



import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 单例+工厂模式
 */
public class DataGeneratorFactory {



    //这个写法之后要详细了解下
    private static final Map<MockTypeEnum,DataGenerator> DATA_GENERATOR_POOL=new HashMap<MockTypeEnum,DataGenerator>(){{
        put(MockTypeEnum.NONE,new DefaultDataGenerator());
        put(MockTypeEnum.FIXED,new FixedDataGenerator());
        put(MockTypeEnum.DICT,new DictGenerator());
        put(MockTypeEnum.INCREASE,new IncreaseDataGenerator());
        put(MockTypeEnum.RANDOM,new RandomDataGenerator());
        put(MockTypeEnum.RULE,new RuleDataGenerator());
    }};


    public static DataGenerator getDataGenerator(MockTypeEnum mockTypeEnum){
         mockTypeEnum = Optional.ofNullable(mockTypeEnum).orElse(MockTypeEnum.NONE);
         return DATA_GENERATOR_POOL.get(mockTypeEnum);
    }
}
