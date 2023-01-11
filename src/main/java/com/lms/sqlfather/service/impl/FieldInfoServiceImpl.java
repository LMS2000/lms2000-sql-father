package com.lms.sqlfather.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.lms.sqlfather.common.ErrorCode;
import com.lms.sqlfather.core.GeneratorFacade;
import com.lms.sqlfather.core.schema.TableSchema;
import com.lms.sqlfather.exception.BusinessException;
import com.lms.sqlfather.mapper.FieldInfoMapper;
import com.lms.sqlfather.mapper.UserMapper;
import com.lms.sqlfather.model.entity.FieldInfo;
import com.lms.sqlfather.model.entity.User;
import com.lms.sqlfather.model.enums.ReviewStatusEnum;
import com.lms.sqlfather.service.FieldInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class FieldInfoServiceImpl extends ServiceImpl<FieldInfoMapper, FieldInfo> implements FieldInfoService {
    private final static Gson GSON = new Gson();
    @Override
    public void validAndHandleFieldInfo(FieldInfo fieldInfo, boolean add) {
        if (fieldInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String content = fieldInfo.getContent();
        String name = fieldInfo.getName();
        Integer reviewStatus = fieldInfo.getReviewStatus();
        // 创建时，所有参数必须非空
        if (add && StringUtils.isAnyBlank(name, content)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isNotBlank(name) && name.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "名称过长");
        }
        if (StringUtils.isNotBlank(content)) {
            if (content.length() > 20000) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
            }
            // 校验字段内容
            try {
                TableSchema.Field field = GSON.fromJson(content, TableSchema.Field.class);
                GeneratorFacade.validField(field);
                // 填充 fieldName
                fieldInfo.setFieldName(field.getFieldName());
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容格式错误");
            }
        }
        if (reviewStatus != null && !ReviewStatusEnum.getValues().contains(reviewStatus)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
    }
}
