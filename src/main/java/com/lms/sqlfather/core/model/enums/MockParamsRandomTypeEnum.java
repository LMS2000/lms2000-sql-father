package com.lms.sqlfather.core.model.enums;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 模拟参数随机类型枚举
 *
 */
public enum MockParamsRandomTypeEnum {

    STRING("字符串"),
    NAME("人名"),
    CITY("城市"),
    URL("网址"),
    EMAIL("邮箱"),
    IP("IP"),
    INTEGER("整数"),
    DECIMAL("小数"),
    UNIVERSITY("大学"),
    DATE("日期"),
    TIMESTAMP("时间戳"),
    IDENTITY("身份证"),

    NICKNAME("昵称"),

    BANK_CARD("银行卡"),
    CREDIT_CARD("信用卡"),

    CONTACT_ADDRESS("联系地址"),

    LONGITUDE("经度"),

    LATITUDE("纬度"),

    EDUCATION_BACKGROUND("学历"),

    MAJOR("专业"),

    COMPANY("公司名字"),

    DEPARTMENT("公司部门"),

    LICENSE_PLATE_NUMBER("车牌号"),

    NATION("民族"),

    BOOLEAN("标识值(0|1)"),

    PERCENT("百分比"),


    PHONE("手机号");


    //更新更多的模拟数据类型： 身份证，昵称，银行卡号，信用卡号，联系地址，经度，维度，学历，学校专业，公司名字，公司部门，车牌号，
    // 民族 标识值(0|1) 百分比

    private final String value;

    MockParamsRandomTypeEnum(String value) {
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(MockParamsRandomTypeEnum::getValue).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static MockParamsRandomTypeEnum getEnumByValue(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        for (MockParamsRandomTypeEnum mockTypeEnum : MockParamsRandomTypeEnum.values()) {
            if (mockTypeEnum.value.equals(value)) {
                return mockTypeEnum;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }
}
