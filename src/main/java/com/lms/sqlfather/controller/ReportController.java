package com.lms.sqlfather.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lms.contants.HttpCode;
import com.lms.result.EnableResponseAdvice;
import com.lms.sqlfather.annotation.AuthCheck;
import com.lms.sqlfather.common.DeleteRequest;
import com.lms.sqlfather.constant.CommonConstant;
import com.lms.sqlfather.exception.BusinessException;
import com.lms.sqlfather.model.dto.ReportAddRequest;
import com.lms.sqlfather.model.dto.ReportQueryRequest;
import com.lms.sqlfather.model.dto.ReportUpdateRequest;
import com.lms.sqlfather.model.entity.Dict;
import com.lms.sqlfather.model.entity.Report;
import com.lms.sqlfather.model.entity.User;
import com.lms.sqlfather.model.enums.ReportStatusEnum;
import com.lms.sqlfather.service.DictService;
import com.lms.sqlfather.service.ReportService;
import com.lms.sqlfather.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/report")
@EnableResponseAdvice
public class ReportController {


    @Resource
    private ReportService reportService;

    @Resource
    private UserService userService;


    @Resource
    private DictService dictService;
    // region 增删改查

    /**
     * 创建
     *
     * @param reportAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public Long addReport(@RequestBody ReportAddRequest reportAddRequest, HttpServletRequest request) {
        BusinessException.throwIf(reportAddRequest == null);
        Report report = new Report();
        BeanUtils.copyProperties(reportAddRequest, report);
        reportService.validReport(report, true);
        User loginUser = userService.getLoginUser(request);
        Dict dict = dictService.getById(report.getReportedId());
        BusinessException.throwIf(dict == null);
        report.setReportedUserId(dict.getUserId());
        report.setUserId(loginUser.getId());
        report.setStatus(ReportStatusEnum.DEFAULT.getValue());
        boolean result = reportService.save(report);
        BusinessException.throwIf(!result, HttpCode.OPERATION_ERROR);
        return report.getId();

    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public Boolean deleteReport(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {

        BusinessException.throwIf(deleteRequest == null || deleteRequest.getId() <= 0);
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Report oldReport = reportService.getById(id);
        BusinessException.throwIf(oldReport == null,HttpCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        BusinessException.throwIf(!oldReport.getUserId().equals(user.getId()) && !userService.isAdmin(request));
        return reportService.removeById(id);

    }

    /**
     * 更新（仅管理员）
     *
     * @param reportUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = "admin")
    public Boolean updateReport(@RequestBody ReportUpdateRequest reportUpdateRequest) {

        BusinessException.throwIf(reportUpdateRequest == null || reportUpdateRequest.getId() <= 0);
        Report report = new Report();
        BeanUtils.copyProperties(reportUpdateRequest, report);
        reportService.validReport(report, false);
        long id = reportUpdateRequest.getId();
        // 判断是否存在
        Report oldReport = reportService.getById(id);
        BusinessException.throwIf(oldReport == null,HttpCode.NOT_FOUND_ERROR);
        return reportService.updateById(report);

    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public Report getReportById(@Positive(message = "id不合法") Long id) {
        return reportService.getById(id);
    }

    /**
     * 获取列表（仅管理员可使用）
     *
     * @param reportQueryRequest
     * @return
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/list")
    public List<Report> listReport(ReportQueryRequest reportQueryRequest) {
        Report reportQuery = new Report();
        if (reportQueryRequest != null) {
            BeanUtils.copyProperties(reportQueryRequest, reportQuery);
        }
        QueryWrapper<Report> queryWrapper = new QueryWrapper<>(reportQuery);
        return reportService.list(queryWrapper);

    }

    /**
     * 分页获取列表
     *
     * @param reportQueryRequest
     * @return
     */
    @GetMapping("/list/page")
    public Page<Report> listReportByPage(ReportQueryRequest reportQueryRequest) {

        BusinessException.throwIf(reportQueryRequest == null);
        Report reportQuery = new Report();
        BeanUtils.copyProperties(reportQueryRequest, reportQuery);
        long current = reportQueryRequest.getCurrent();
        long size = reportQueryRequest.getPageSize();
        String sortField = reportQueryRequest.getSortField();
        String sortOrder = reportQueryRequest.getSortOrder();
        String content = reportQuery.getContent();
        // content 需支持模糊搜索
        reportQuery.setContent(null);
        // 限制爬虫
        BusinessException.throwIf(size>50);
        QueryWrapper<Report> queryWrapper = new QueryWrapper<>(reportQuery);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return reportService.page(new Page<>(current, size), queryWrapper);

    }




}
