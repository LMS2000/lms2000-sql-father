package com.lms.sqlfather.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.lms.contants.HttpCode;
import com.lms.result.EnableResponseAdvice;
import com.lms.sqlfather.annotation.AuthCheck;
import com.lms.sqlfather.common.DeleteRequest;
import com.lms.sqlfather.constant.CommonConstant;
import com.lms.sqlfather.core.builder.SqlBuilder;
import com.lms.sqlfather.core.schema.TableSchema;
import com.lms.sqlfather.exception.BusinessException;
import com.lms.sqlfather.model.dto.TableInfoAddRequest;
import com.lms.sqlfather.model.dto.TableInfoQueryRequest;
import com.lms.sqlfather.model.dto.TableInfoUpdateRequest;
import com.lms.sqlfather.model.entity.TableInfo;
import com.lms.sqlfather.model.entity.User;
import com.lms.sqlfather.model.enums.ReviewStatusEnum;
import com.lms.sqlfather.service.TableInfoService;
import com.lms.sqlfather.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import org.springframework.web.bind.annotation.*;


import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/table_info")
@EnableResponseAdvice
public class TableInfoController {

    @Resource
    private TableInfoService tableInfoService;

    @Resource
    private UserService userService;

    private final static Gson GSON = new Gson();

    @PostMapping("/add")
    public Long add(@RequestBody TableInfoAddRequest tableInfoAddRequest,
                                  HttpServletRequest request) {

       BusinessException.throwIf(tableInfoAddRequest == null);
        TableInfo tableInfo = new TableInfo();
        BeanUtils.copyProperties(tableInfoAddRequest, tableInfo);

        tableInfoService.validAndHandleTableInfo(tableInfo, true);
        User loginUser = userService.getLoginUser(request);
        tableInfo.setUserId(loginUser.getId());
        boolean result = tableInfoService.save(tableInfo);
        BusinessException.throwIf(!result, HttpCode.OPERATION_ERROR);
        return tableInfo.getId();
    }

    @PostMapping("/delete")
    public Boolean delete(@RequestBody DeleteRequest deleteRequest
            , HttpServletRequest request) {

        BusinessException.throwIf(deleteRequest == null || deleteRequest.getId() <= 0);
        TableInfo byId = tableInfoService.getById(deleteRequest.getId());
        BusinessException.throwIf(byId == null);
        User loginUser = userService.getLoginUser(request);

        BusinessException.throwIf(!loginUser.getId().equals(byId.getUserId()) && !userService.isAdmin(request),
                HttpCode.NO_AUTH_ERROR);
      return tableInfoService.removeById(byId.getId());

    }

    /**
     * 仅管理员操作
     *
     * @param tableInfoUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = "admin")
    public Boolean update(@RequestBody TableInfoUpdateRequest tableInfoUpdateRequest
            , HttpServletRequest request) {
        BusinessException.throwIf(tableInfoUpdateRequest == null);
        TableInfo tableInfo = new TableInfo();
        BeanUtils.copyProperties(tableInfoUpdateRequest, tableInfo);
        tableInfoService.validAndHandleTableInfo(tableInfo, false);
        TableInfo byId = tableInfoService.getById(tableInfo.getId());
        BusinessException.throwIf(byId == null,HttpCode.NOT_FOUND_ERROR);
       return tableInfoService.updateById(tableInfo);

    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public TableInfo getTableInfoById(@Positive(message = "id不合法") Long id) {

       return tableInfoService.getById(id);

    }

    /**
     * 获取列表（仅管理员可使用）
     *
     * @param tableInfoQueryRequest
     * @return
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/list")
    public List<TableInfo> listTableInfo(TableInfoQueryRequest tableInfoQueryRequest) {
       return tableInfoService.list(getQueryWrapper(tableInfoQueryRequest));

    }

    /**
     * 分页获取列表
     *
     * @param tableInfoQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    public Page<TableInfo> listTableInfoByPage(TableInfoQueryRequest tableInfoQueryRequest,
                                                             HttpServletRequest request) {
        long current = tableInfoQueryRequest.getCurrent();
        long size = tableInfoQueryRequest.getPageSize();
        // 限制爬虫
        BusinessException.throwIf(size>20);
        return tableInfoService.page(new Page<>(current, size),
                getQueryWrapper(tableInfoQueryRequest));
    }

    /**
     * @param tableInfoQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/my/list")
    public List<TableInfo> listMyTableInfo(TableInfoQueryRequest tableInfoQueryRequest,
                                                         HttpServletRequest request) {

        TableInfo tableInfoQuery = new TableInfo();
        if (tableInfoQueryRequest != null) {
            BeanUtils.copyProperties(tableInfoQueryRequest, tableInfoQuery);
        }
        QueryWrapper<TableInfo> queryWrapper = getQueryWrapper(tableInfoQueryRequest);
        String[] fields = new String[]{"id", "name"};
        queryWrapper.eq("review_status", ReviewStatusEnum.PASS.getValue());
        queryWrapper.select(fields);
        List<TableInfo> tableInfoList = tableInfoService.list(queryWrapper);

        try {
            User loginUser = userService.getLoginUser(request);
            tableInfoQuery.setReviewStatus(null);
            tableInfoQuery.setUserId(loginUser.getId());
            queryWrapper = new QueryWrapper<>(tableInfoQuery);
            queryWrapper.select(fields);
            tableInfoList.addAll(tableInfoService.list(queryWrapper));
        } catch (Exception ex) {

        }
        //stream 去重
        return tableInfoList.stream().collect(Collectors
                .collectingAndThen(Collectors.toCollection(() ->
                        new TreeSet<>(Comparator.comparing(TableInfo::getId))
                ), ArrayList::new));


    }

    /***
     *
     * @param tableInfoQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/my/list/page")
    public Page<TableInfo> listMyTableInfoByPage(TableInfoQueryRequest tableInfoQueryRequest
            ,HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        long current = tableInfoQueryRequest.getCurrent();
        long size = tableInfoQueryRequest.getPageSize();
        BusinessException.throwIf(size>20);
        QueryWrapper<TableInfo> queryWrapper = getQueryWrapper(tableInfoQueryRequest);
        queryWrapper.eq("userId",loginUser.getId()).or().eq("reviewStatus",ReviewStatusEnum.PASS.getValue());
        return tableInfoService.page(new Page<>(current, size), queryWrapper);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param tableInfoQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/my/add/list/page")
    public Page<TableInfo> listMyAddTableInfoByPage(TableInfoQueryRequest tableInfoQueryRequest,
                                                                  HttpServletRequest request) {
        BusinessException.throwIf(tableInfoQueryRequest == null);
        User loginUser = userService.getLoginUser(request);
        tableInfoQueryRequest.setUserId(loginUser.getId());
        long current = tableInfoQueryRequest.getCurrent();
        long size = tableInfoQueryRequest.getPageSize();
        // 限制爬虫

        BusinessException.throwIf(size>20);
       return tableInfoService.page(new Page<>(current, size),
                getQueryWrapper(tableInfoQueryRequest));

    }

    // endregion

    /**
     * 生成创建表的 SQL
     *
     * @param id
     * @return
     */
    @PostMapping("/generate/sql")
    public String generateCreateSql(@RequestBody @Positive(message = "id不合法") Long id) {

        TableInfo tableInfo = tableInfoService.getById(id);

        BusinessException.throwIf(tableInfo == null,HttpCode.NOT_FOUND_ERROR);
        TableSchema tableSchema = GSON.fromJson(tableInfo.getContent(), TableSchema.class);
        SqlBuilder sqlBuilder = new SqlBuilder();
        return sqlBuilder.buildCreateTableSql(tableSchema);
    }




    private QueryWrapper<TableInfo> getQueryWrapper(TableInfoQueryRequest tableInfoQueryRequest) {
        BusinessException.throwIf(tableInfoQueryRequest == null,HttpCode.NOT_FOUND_ERROR);
        TableInfo tableInfoQuery = new TableInfo();

        BeanUtils.copyProperties(tableInfoQueryRequest, tableInfoQuery);

        String sortField = tableInfoQueryRequest.getSortField();
        String sortOrder = tableInfoQueryRequest.getSortOrder();
        String name = tableInfoQuery.getName();
        String content = tableInfoQuery.getContent();
        tableInfoQuery.setContent(null);
        tableInfoQuery.setName(null);
        QueryWrapper<TableInfo> wrapper = new QueryWrapper<>(tableInfoQuery);
        wrapper.like(StringUtils.isNotBlank(name), "name", name);
        wrapper.like(StringUtils.isNotBlank(content), "content", content);
        wrapper.orderBy(StringUtils.isNotBlank(sortField)
                , sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return wrapper;
    }


}
