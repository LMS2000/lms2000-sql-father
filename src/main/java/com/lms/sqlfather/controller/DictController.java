package com.lms.sqlfather.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lms.contants.HttpCode;
import com.lms.result.EnableResponseAdvice;
import com.lms.sqlfather.annotation.AuthCheck;
import com.lms.sqlfather.common.DeleteRequest;
import com.lms.sqlfather.constant.CommonConstant;
import com.lms.sqlfather.core.GeneratorFacade;
import com.lms.sqlfather.core.model.enums.MockTypeEnum;
import com.lms.sqlfather.core.model.vo.GenerateVO;
import com.lms.sqlfather.core.schema.TableSchema;
import com.lms.sqlfather.exception.BusinessException;
import com.lms.sqlfather.model.dto.DictAddRequest;
import com.lms.sqlfather.model.dto.DictQueryRequest;
import com.lms.sqlfather.model.dto.DictUpdateRequest;
import com.lms.sqlfather.model.entity.Dict;
import com.lms.sqlfather.model.entity.User;
import com.lms.sqlfather.model.enums.ReviewStatusEnum;
import com.lms.sqlfather.service.DictService;
import com.lms.sqlfather.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import com.lms.sqlfather.core.schema.TableSchema.Field;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dict")
@EnableResponseAdvice
public class DictController {


    @Resource
    private UserService userService;

    @Resource
    private DictService dictService;
    /**
     * 创建
     *
     * @param dictAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public Long addDict(@RequestBody DictAddRequest dictAddRequest, HttpServletRequest request) {

        BusinessException.throwIf(dictAddRequest==null, HttpCode.PARAMS_ERROR);
        Dict dict = new Dict();
        BeanUtils.copyProperties(dictAddRequest, dict);
        // 校验
        dictService.validAndHandleDict(dict, true);
        User loginUser = userService.getLoginUser(request);
        dict.setUserId(loginUser.getId());
        boolean result = dictService.save(dict);
        BusinessException.throwIf(!result, HttpCode.OPERATION_ERROR);
        return dict.getId();
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public Boolean deleteDict(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {

        BusinessException.throwIf(deleteRequest == null || deleteRequest.getId() <= 0,
                HttpCode.PARAMS_ERROR);
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Dict oldDict = dictService.getById(id);
        BusinessException.throwIf(oldDict == null,HttpCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        BusinessException.throwIf(!oldDict.getUserId().equals(user.getId()) && !userService.isAdmin(request),
                HttpCode.NO_AUTH_ERROR);
       return dictService.removeById(id);

    }

    /**
     * 更新（仅管理员）
     *
     * @param dictUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = "admin")
    public Boolean updateDict(@RequestBody DictUpdateRequest dictUpdateRequest) {

        BusinessException.throwIf(dictUpdateRequest == null || dictUpdateRequest.getId() <= 0,
                HttpCode.PARAMS_ERROR);
        Dict dict = new Dict();
        BeanUtils.copyProperties(dictUpdateRequest, dict);
        // 参数校验
        dictService.validAndHandleDict(dict, false);
        long id = dictUpdateRequest.getId();
        // 判断是否存在
        Dict oldDict = dictService.getById(id);

        BusinessException.throwIf(oldDict == null,HttpCode.NOT_FOUND_ERROR);
        return dictService.updateById(dict);

    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public Dict getDictById(@Positive(message = "id不合法") Long id) {
        return dictService.getById(id);

    }

    /**
     * 获取列表（仅管理员可使用）
     *
     * @param dictQueryRequest
     * @return
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/list")
    public List<Dict> listDict(DictQueryRequest dictQueryRequest) {
        return dictService.list(getQueryWrapper(dictQueryRequest));

    }

    /**
     * 分页获取列表
     *
     * @param dictQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    public Page<Dict> listDictByPage(DictQueryRequest dictQueryRequest,
                                                   HttpServletRequest request) {
        long current = dictQueryRequest.getCurrent();
        long size = dictQueryRequest.getPageSize();
        // 限制爬虫

        BusinessException.throwIf(size>20,HttpCode.PARAMS_ERROR);
        return dictService.page(new Page<>(current, size),
                getQueryWrapper(dictQueryRequest));

    }

    /**
     * 获取当前用户可选的全部资源列表（只返回 id 和名称）
     *
     * @param dictQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/my/list")
    public List<Dict> listMyDict(DictQueryRequest dictQueryRequest,
                                               HttpServletRequest request) {

        BusinessException.throwIf(dictQueryRequest == null,HttpCode.PARAMS_ERROR);
        // 先查询所有审核通过的
        dictQueryRequest.setReviewStatus(ReviewStatusEnum.PASS.getValue());
        Dict dictQuery = new Dict();
        QueryWrapper<Dict> queryWrapper = getQueryWrapper(dictQueryRequest);
        final String[] fields = new String[]{"id", "name"};
        queryWrapper.select(fields);
        List<Dict> dictList = dictService.list(queryWrapper);
        // 再查所有本人的
        try {
            User loginUser = userService.getLoginUser(request);
            dictQuery.setReviewStatus(null);
            dictQuery.setUserId(loginUser.getId());
            queryWrapper = new QueryWrapper<>(dictQuery);
            queryWrapper.select(fields);
            dictList.addAll(dictService.list(queryWrapper));
        } catch (Exception e) {
            // 未登录
        }
        // 根据 id 去重
        return dictList.stream().collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Dict::getId))), ArrayList::new));

    }

    /**
     * 分页获取当前用户可选的资源列表
     *
     * @param dictQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/my/list/page")
    public Page<Dict> listMyDictByPage(DictQueryRequest dictQueryRequest,
                                                     HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        long current = dictQueryRequest.getCurrent();
        long size = dictQueryRequest.getPageSize();
        // 限制爬虫
        BusinessException.throwIf(size>20,HttpCode.PARAMS_ERROR);
        QueryWrapper<Dict> queryWrapper = getQueryWrapper(dictQueryRequest);
        queryWrapper.eq("user_id", loginUser.getId())
                .or()
                .eq("review_status", ReviewStatusEnum.PASS.getValue());
        return dictService.page(new Page<>(current, size), queryWrapper);

    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param dictQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/my/add/list/page")
    public Page<Dict> listMyAddDictByPage(DictQueryRequest dictQueryRequest,
                                                        HttpServletRequest request) {
       BusinessException.throwIf(dictQueryRequest == null,HttpCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        dictQueryRequest.setUserId(loginUser.getId());
        long current = dictQueryRequest.getCurrent();
        long size = dictQueryRequest.getPageSize();
        // 限制爬虫
        BusinessException.throwIf(size>20,HttpCode.PARAMS_ERROR);
       return dictService.page(new Page<>(current, size),
                getQueryWrapper(dictQueryRequest));

    }


    /**
     * 生成创建表的 SQL
     *
     * @param id
     * @return
     */
    //生成的sql表就是只有id,data两个字段，这个接口是用来生成用户自己生成的字段随机内容
    @PostMapping("/generate/sql")
    public GenerateVO generateCreateSql(@RequestBody @Positive(message = "id不合法") long id) {
        Dict byId = dictService.getById(id);
        BusinessException.throwIf(byId==null,HttpCode.NOT_FOUND_ERROR);
        String name = byId.getName();
        TableSchema tableSchema=new TableSchema();
        tableSchema.setTableName("dict");
        tableSchema.setTableComment(name);
         Field idField = new Field();
        idField.setFieldName("id");
        idField.setFieldType("bigint");
        idField.setNotNull(true);
        idField.setComment("id");
        idField.setPrimaryKey(true);
        idField.setAutoIncrement(true);
        Field dataField = new Field();
        dataField.setFieldName("data");
        dataField.setFieldType("text");
        dataField.setComment("数据");
        dataField.setMockType(MockTypeEnum.DICT.getValue());
        dataField.setMockParams(String.valueOf(id));
        List<Field> fieldList=new ArrayList<>();
        fieldList.add(idField);
        fieldList.add(dataField);
        tableSchema.setFieldList(fieldList);
        return GeneratorFacade.generateAll(tableSchema);

    }


    /**
     * 获取查询包装类
     *
     * @param dictQueryRequest
     * @return
     */
    private QueryWrapper<Dict> getQueryWrapper(DictQueryRequest dictQueryRequest) {

        BusinessException.throwIf(dictQueryRequest == null,HttpCode.PARAMS_ERROR,"请求参数为空");
        Dict dictQuery = new Dict();
        BeanUtils.copyProperties(dictQueryRequest, dictQuery);
        String sortField = dictQueryRequest.getSortField();
        String sortOrder = dictQueryRequest.getSortOrder();
        String name = dictQuery.getName();
        String content = dictQuery.getContent();
        // name、content 需支持模糊搜索
        dictQuery.setName(null);
        dictQuery.setContent(null);
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>(dictQuery);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

}
