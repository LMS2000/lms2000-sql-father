package com.lms.sqlfather.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.lms.contants.HttpCode;
import com.lms.sqlfather.core.GeneratorFacade;
import com.lms.sqlfather.core.schema.TableSchema;
import com.lms.sqlfather.exception.BusinessException;
import com.lms.sqlfather.mapper.TableInfoMapper;
import com.lms.sqlfather.model.entity.TableInfo;
import com.lms.sqlfather.model.enums.ReviewStatusEnum;
import com.lms.sqlfather.service.TableInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class TableInfoServiceImpl extends ServiceImpl<TableInfoMapper, TableInfo> implements TableInfoService {

     private static final Gson GSON  =new Gson();

    @Override
    public void validAndHandleTableInfo(TableInfo tableInfo, boolean add) {
        BusinessException.throwIf(tableInfo==null);
        String content = tableInfo.getContent();
        Integer reviewStatus = tableInfo.getReviewStatus();
        String name = tableInfo.getName();

        BusinessException.throwIf(add& StringUtils.isAnyBlank(name,content));

        BusinessException.throwIf(StringUtils.isNotBlank(name)&&name.length()>30);

        if(StringUtils.isNotBlank(content)){

            BusinessException.throwIf(content.length()>20000);
            try{
                TableSchema tableSchema = GSON.fromJson(content, TableSchema.class);
                GeneratorFacade.validSchema(tableSchema);
            }catch (Exception ex){
                   throw new BusinessException(HttpCode.PARAMS_ERROR,"内容错误");
            }

        }
        BusinessException.throwIf(reviewStatus!=null&& !ReviewStatusEnum.getValues().contains(reviewStatus));
    }
}
