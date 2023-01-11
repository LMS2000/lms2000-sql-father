package com.lms.sqlfather.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 根据 SQL 生成请求体
 *
 * @author https://github.com/liyupi
 */
@Data
public class GenerateBySqlRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    private String sql;
}
