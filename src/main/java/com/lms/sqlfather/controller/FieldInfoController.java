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
import com.lms.sqlfather.model.dto.FieldInfoAddRequest;
import com.lms.sqlfather.model.dto.FieldInfoQueryRequest;
import com.lms.sqlfather.model.dto.FieldInfoUpdateRequest;
import com.lms.sqlfather.model.entity.FieldInfo;
import com.lms.sqlfather.model.entity.User;
import com.lms.sqlfather.model.enums.ReviewStatusEnum;
import com.lms.sqlfather.service.FieldInfoService;
import com.lms.sqlfather.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.lms.sqlfather.core.schema.TableSchema.Field;

@RestController
@RequestMapping("/field_info")
@EnableResponseAdvice
public class FieldInfoController {

    @Resource
    private FieldInfoService fieldInfoService;

    @Resource
    private UserService userService;

    private static final Gson GSON = new Gson();

    /**
     * 创建
     *
     * @param fieldInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public Long addFieldInfo(@RequestBody FieldInfoAddRequest fieldInfoAddRequest,
                                           HttpServletRequest request) {
        BusinessException.throwIf(fieldInfoAddRequest == null);
        FieldInfo fieldInfo = new FieldInfo();
        BeanUtils.copyProperties(fieldInfoAddRequest, fieldInfo);
        // 校验
        fieldInfoService.validAndHandleFieldInfo(fieldInfo, true);
        User loginUser = userService.getLoginUser(request);
        fieldInfo.setUserId(loginUser.getId());
        boolean result = fieldInfoService.save(fieldInfo);

        BusinessException.throwIf(!result);
        return fieldInfo.getId();
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public Boolean deleteFieldInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {

        BusinessException.throwIf(deleteRequest == null || deleteRequest.getId() <= 0,
                HttpCode.PARAMS_ERROR);
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        FieldInfo oldFieldInfo = fieldInfoService.getById(id);
        BusinessException.throwIf(oldFieldInfo == null);
        // 仅本人或管理员可删除
        BusinessException.throwIf(!oldFieldInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request));
        return fieldInfoService.removeById(id);

    }

    /**
     * 更新（仅管理员）
     *
     * @param fieldInfoUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = "admin")
    public Boolean updateFieldInfo(@RequestBody FieldInfoUpdateRequest fieldInfoUpdateRequest) {

        BusinessException.throwIf(fieldInfoUpdateRequest == null || fieldInfoUpdateRequest.getId() <= 0);
        FieldInfo fieldInfo = new FieldInfo();
        BeanUtils.copyProperties(fieldInfoUpdateRequest, fieldInfo);
        // 参数校验
        fieldInfoService.validAndHandleFieldInfo(fieldInfo, false);
        long id = fieldInfoUpdateRequest.getId();
        // 判断是否存在
        FieldInfo oldFieldInfo = fieldInfoService.getById(id);

        BusinessException.throwIf(oldFieldInfo == null);
        return fieldInfoService.updateById(fieldInfo);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public FieldInfo getFieldInfoById(@Positive(message = "id不合法") Long id) {

       return fieldInfoService.getById(id);

    }

    /**
     * 获取列表（仅管理员可使用）
     *
     * @param fieldInfoQueryRequest
     * @return
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/list")
    public List<FieldInfo> listFieldInfo(FieldInfoQueryRequest fieldInfoQueryRequest) {
        return  fieldInfoService.list(getQueryWrapper(fieldInfoQueryRequest));

    }

    /**
     * 分页获取列表
     *
     * @param fieldInfoQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    public Page<FieldInfo> listFieldInfoByPage(FieldInfoQueryRequest fieldInfoQueryRequest,
                                                             HttpServletRequest request) {
        long current = fieldInfoQueryRequest.getCurrent();
        long size = fieldInfoQueryRequest.getPageSize();
        // 限制爬虫
        BusinessException.throwIf(size>20);
        return fieldInfoService.page(new Page<>(current, size),
                getQueryWrapper(fieldInfoQueryRequest));

    }

    /**
     * 获取当前用户可选的全部资源列表（只返回 id 和名称）
     *
     * @param fieldInfoQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/my/list")
    public List<FieldInfo> listMyFieldInfo(FieldInfoQueryRequest fieldInfoQueryRequest,
                                                         HttpServletRequest request) {
        FieldInfo fieldInfoQuery = new FieldInfo();
        if (fieldInfoQueryRequest != null) {
            BeanUtils.copyProperties(fieldInfoQueryRequest, fieldInfoQuery);
        }
        // 先查询所有审核通过的
        fieldInfoQuery.setReviewStatus(ReviewStatusEnum.PASS.getValue());
        QueryWrapper<FieldInfo> queryWrapper = getQueryWrapper(fieldInfoQueryRequest);
        final String[] fields = new String[]{"id", "name"};
        queryWrapper.select(fields);
        List<FieldInfo> fieldInfoList = fieldInfoService.list(queryWrapper);
        // 再查所有本人的
        try {
            User loginUser = userService.getLoginUser(request);
            fieldInfoQuery.setReviewStatus(null);
            fieldInfoQuery.setUserId(loginUser.getId());
            queryWrapper = new QueryWrapper<>(fieldInfoQuery);
            queryWrapper.select(fields);
            fieldInfoList.addAll(fieldInfoService.list(queryWrapper));
        } catch (Exception e) {
            // 未登录
        }
        // 根据 id 去重
       return fieldInfoList.stream().collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(FieldInfo::getId))), ArrayList::new));
    }

    /**
     * 分页获取当前用户可选的资源列表
     *
     * @param fieldInfoQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/my/list/page")
    public Page<FieldInfo> listMyFieldInfoByPage(FieldInfoQueryRequest fieldInfoQueryRequest,
                                                               HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        long current = fieldInfoQueryRequest.getCurrent();
        long size = fieldInfoQueryRequest.getPageSize();
        // 限制爬虫
        BusinessException.throwIf(size > 20);
        QueryWrapper<FieldInfo> queryWrapper = getQueryWrapper(fieldInfoQueryRequest);
        queryWrapper.eq("userId", loginUser.getId())
                .or()
                .eq("reviewStatus", ReviewStatusEnum.PASS.getValue());
        return fieldInfoService.page(new Page<>(current, size), queryWrapper);

    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param fieldInfoQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/my/add/list/page")
    public Page<FieldInfo> listMyAddFieldInfoByPage(FieldInfoQueryRequest fieldInfoQueryRequest,
                                                                  HttpServletRequest request) {

        BusinessException.throwIf(fieldInfoQueryRequest == null);
        User loginUser = userService.getLoginUser(request);
        fieldInfoQueryRequest.setUserId(loginUser.getId());
        long current = fieldInfoQueryRequest.getCurrent();
        long size = fieldInfoQueryRequest.getPageSize();
        // 限制爬虫
        BusinessException.throwIf(size > 20);
        return fieldInfoService.page(new Page<>(current, size),
                getQueryWrapper(fieldInfoQueryRequest));

    }

    /**
     * 生成创建字段的 SQL
     *
     * @param id
     * @return
     */
    @PostMapping("/generate/sql")
    public String generateCreateSql(@RequestBody @Positive(message = "id不合法") Long id) {


        FieldInfo byId = fieldInfoService.getById(id);

        BusinessException.throwIf(byId==null);
        Field field = GSON.fromJson(byId.getContent(), Field.class);
        SqlBuilder sqlBuilder=new SqlBuilder();
        return sqlBuilder.buildCreateFieldSql(field);
    }


    /**
     * 获取查询包装类
     *
     * @param fieldInfoQueryRequest
     * @return
     */
    private QueryWrapper<FieldInfo> getQueryWrapper(FieldInfoQueryRequest fieldInfoQueryRequest) {
        BusinessException.throwIf(fieldInfoQueryRequest == null,HttpCode.PARAMS_ERROR,
                "请求参数为空");
        FieldInfo fieldInfoQuery = new FieldInfo();
        BeanUtils.copyProperties(fieldInfoQueryRequest, fieldInfoQuery);
        String searchName = fieldInfoQueryRequest.getSearchName();
        String sortField = fieldInfoQueryRequest.getSortField();
        String sortOrder = fieldInfoQueryRequest.getSortOrder();
        String name = fieldInfoQuery.getName();
        String content = fieldInfoQuery.getContent();
        String fieldName = fieldInfoQuery.getFieldName();
        // name、fieldName、content 需支持模糊搜索
        fieldInfoQuery.setName(null);
        fieldInfoQuery.setFieldName(null);
        fieldInfoQuery.setContent(null);
        QueryWrapper<FieldInfo> queryWrapper = new QueryWrapper<>(fieldInfoQuery);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.like(StringUtils.isNotBlank(fieldName), "field_name", fieldName);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        // 同时按 name、fieldName 搜索
        if (StringUtils.isNotBlank(searchName)) {
            queryWrapper.like("name", searchName).or().like("field_name", searchName);
        }
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }
}
