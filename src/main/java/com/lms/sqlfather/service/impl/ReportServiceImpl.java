package com.lms.sqlfather.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lms.sqlfather.common.ErrorCode;
import com.lms.sqlfather.exception.BusinessException;
import com.lms.sqlfather.mapper.ReportMapper;
import com.lms.sqlfather.mapper.UserMapper;
import com.lms.sqlfather.model.entity.Report;
import com.lms.sqlfather.model.entity.User;
import com.lms.sqlfather.model.enums.ReportStatusEnum;
import com.lms.sqlfather.service.ReportService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {
    @Override
    public void validReport(Report report, boolean add) {
      if(report==null){
          throw new BusinessException(ErrorCode.PARAMS_ERROR);
      }
        String content = report.getContent();
        Integer status = report.getStatus();
        Long reportedId = report.getReportedId();
        if(StringUtils.isNotBlank(content)&&content.length()>1024){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"内容过长");
        }
        if(add){
            if(reportedId==null||reportedId<=0){
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }else{
            if(status!=null&& !ReportStatusEnum.getValues().contains(status)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }
    }
}
