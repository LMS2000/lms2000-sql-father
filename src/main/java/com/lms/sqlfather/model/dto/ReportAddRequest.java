package com.lms.sqlfather.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建请求
 *
 * @TableName report
 */
@Data
public class ReportAddRequest implements Serializable {

    /**
     * 内容
     */
    private String content;

    /**
     * 举报实体类型（0-词库）
     */
    private Integer type;

    /**
     * 被举报对象 id
     */
    private Long reportedId;

    private static final long serialVersionUID = 1L;
}