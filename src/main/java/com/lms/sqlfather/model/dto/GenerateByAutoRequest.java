package com.lms.sqlfather.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 智能生成请求体
 *
 */
@Data
public class GenerateByAutoRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    private String content;
}
