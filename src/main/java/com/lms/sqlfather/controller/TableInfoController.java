package com.lms.sqlfather.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.google.gson.Gson;
import com.lms.sqlfather.annotation.AuthCheck;
import com.lms.sqlfather.common.BaseResponse;
import com.lms.sqlfather.common.DeleteRequest;
import com.lms.sqlfather.common.ErrorCode;
import com.lms.sqlfather.common.ResultUtils;
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
import org.apache.logging.log4j.util.PropertySource;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;
import sun.awt.IconInfo;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/table_info")
public class TableInfoController {

    @Resource
    private TableInfoService tableInfoService;

    @Resource
    private UserService userService;

    private final static Gson GSON = new Gson();

    @PostMapping("/add")
    public BaseResponse<Long> add(@RequestBody TableInfoAddRequest tableInfoAddRequest,
                                  HttpServletRequest request) {
        if (tableInfoAddRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        TableInfo tableInfo = new TableInfo();
        BeanUtils.copyProperties(tableInfoAddRequest, tableInfo);

        tableInfoService.validAndHandleTableInfo(tableInfo, true);
        User loginUser = userService.getLoginUser(request);
        tableInfo.setUserId(loginUser.getId());
        boolean result = tableInfoService.save(tableInfo);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(tableInfo.getId());
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> delete(@RequestBody DeleteRequest deleteRequest
            , HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        TableInfo byId = tableInfoService.getById(deleteRequest.getId());

        if (byId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "找不到该条记录");
        }
        User loginUser = userService.getLoginUser(request);

        if (!loginUser.getId().equals(byId.getUserId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = tableInfoService.removeById(byId.getId());
        return ResultUtils.success(result);
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
    public BaseResponse<Boolean> update(@RequestBody TableInfoUpdateRequest tableInfoUpdateRequest
            , HttpServletRequest request) {
        if (tableInfoUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        TableInfo tableInfo = new TableInfo();
        BeanUtils.copyProperties(tableInfoUpdateRequest, tableInfo);
        tableInfoService.validAndHandleTableInfo(tableInfo, false);
        TableInfo byId = tableInfoService.getById(tableInfo.getId());
        if (byId == null) throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        boolean result = tableInfoService.updateById(tableInfo);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<TableInfo> getTableInfoById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        TableInfo tableInfo = tableInfoService.getById(id);
        return ResultUtils.success(tableInfo);
    }

    /**
     * 获取列表（仅管理员可使用）
     *
     * @param tableInfoQueryRequest
     * @return
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/list")
    public BaseResponse<List<TableInfo>> listTableInfo(TableInfoQueryRequest tableInfoQueryRequest) {
        List<TableInfo> tableInfoList = tableInfoService.list(getQueryWrapper(tableInfoQueryRequest));
        return ResultUtils.success(tableInfoList);
    }

    /**
     * 分页获取列表
     *
     * @param tableInfoQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<TableInfo>> listTableInfoByPage(TableInfoQueryRequest tableInfoQueryRequest,
                                                             HttpServletRequest request) {
        long current = tableInfoQueryRequest.getCurrent();
        long size = tableInfoQueryRequest.getPageSize();
        // 限制爬虫
        if (size > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<TableInfo> tableInfoPage = tableInfoService.page(new Page<>(current, size),
                getQueryWrapper(tableInfoQueryRequest));
        return ResultUtils.success(tableInfoPage);
    }

    /**
     * @param tableInfoQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/my/list")
    public BaseResponse<List<TableInfo>> listMyTableInfo(TableInfoQueryRequest tableInfoQueryRequest,
                                                         HttpServletRequest request) {

        TableInfo tableInfoQuery = new TableInfo();
        if (tableInfoQueryRequest != null) {
            BeanUtils.copyProperties(tableInfoQueryRequest, tableInfoQuery);
        }
        QueryWrapper<TableInfo> queryWrapper = getQueryWrapper(tableInfoQueryRequest);
        String[] fields = new String[]{"id", "name"};
        queryWrapper.eq("reviewStatus", ReviewStatusEnum.PASS.getValue());
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
        ArrayList<TableInfo> resultList = tableInfoList.stream().collect(Collectors
                .collectingAndThen(Collectors.toCollection(() ->
                        new TreeSet<>(Comparator.comparing(TableInfo::getId))
                ), ArrayList::new));

        return ResultUtils.success(resultList);
    }

    /***
     *
     * @param tableInfoQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/my/list/page")
    public BaseResponse<Page<TableInfo>> listMyTableInfoByPage(TableInfoQueryRequest tableInfoQueryRequest
            ,HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        long current = tableInfoQueryRequest.getCurrent();
        long size = tableInfoQueryRequest.getPageSize();
        if(size>20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<TableInfo> queryWrapper = getQueryWrapper(tableInfoQueryRequest);
        queryWrapper.eq("userId",loginUser.getId()).or().eq("reviewStatus",ReviewStatusEnum.PASS.getValue());
        Page<TableInfo> tableInfoPage = tableInfoService.page(new Page<>(current, size), queryWrapper);
         return ResultUtils.success(tableInfoPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param tableInfoQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/my/add/list/page")
    public BaseResponse<Page<TableInfo>> listMyAddTableInfoByPage(TableInfoQueryRequest tableInfoQueryRequest,
                                                                  HttpServletRequest request) {
        if (tableInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        tableInfoQueryRequest.setUserId(loginUser.getId());
        long current = tableInfoQueryRequest.getCurrent();
        long size = tableInfoQueryRequest.getPageSize();
        // 限制爬虫
        if (size > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<TableInfo> tableInfoPage = tableInfoService.page(new Page<>(current, size),
                getQueryWrapper(tableInfoQueryRequest));
        return ResultUtils.success(tableInfoPage);
    }

    // endregion

    /**
     * 生成创建表的 SQL
     *
     * @param id
     * @return
     */
    @PostMapping("/generate/sql")
    public BaseResponse<String> generateCreateSql(@RequestBody long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        TableInfo tableInfo = tableInfoService.getById(id);
        if (tableInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        TableSchema tableSchema = GSON.fromJson(tableInfo.getContent(), TableSchema.class);
        SqlBuilder sqlBuilder = new SqlBuilder();
        return ResultUtils.success(sqlBuilder.buildCreateTableSql(tableSchema));
    }




    private QueryWrapper<TableInfo> getQueryWrapper(TableInfoQueryRequest tableInfoQueryRequest) {
        if (tableInfoQueryRequest == null) throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
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
