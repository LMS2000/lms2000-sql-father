package com.lms.sqlfather.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lms.sqlfather.model.entity.Report;
import com.lms.sqlfather.model.entity.User;

public interface ReportService extends IService<Report> {

    /**
     * 校验
     *
     * @param report
     * @param add
     */
    void validReport(Report report, boolean add);
}
