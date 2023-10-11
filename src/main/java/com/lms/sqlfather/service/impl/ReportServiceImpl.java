package com.lms.sqlfather.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lms.sqlfather.exception.BusinessException;
import com.lms.sqlfather.mapper.ReportMapper;
import com.lms.sqlfather.model.entity.Report;
import com.lms.sqlfather.model.enums.ReportStatusEnum;
import com.lms.sqlfather.service.ReportService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {
    @Override
    public void validReport(Report report, boolean add) {

      BusinessException.throwIf(report==null);
        String content = report.getContent();
        Integer status = report.getStatus();
        Long reportedId = report.getReportedId();
        BusinessException.throwIf(StringUtils.isNotBlank(content)&&content.length()>1024);
        if(add){

            BusinessException.throwIf(reportedId==null||reportedId<=0);
        }else{
            BusinessException.throwIf(status!=null&& !ReportStatusEnum.getValues().contains(status));
        }
    }
}
