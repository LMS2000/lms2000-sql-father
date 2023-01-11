package com.lms.sqlfather.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.lms.sqlfather.common.ErrorCode;
import com.lms.sqlfather.core.GeneratorFacade;
import com.lms.sqlfather.core.schema.TableSchema;
import com.lms.sqlfather.exception.BusinessException;
import com.lms.sqlfather.mapper.TableInfoMapper;
import com.lms.sqlfather.mapper.UserMapper;
import com.lms.sqlfather.model.entity.TableInfo;
import com.lms.sqlfather.model.entity.User;
import com.lms.sqlfather.model.enums.ReviewStatusEnum;
import com.lms.sqlfather.service.TableInfoService;
import net.bytebuddy.agent.builder.AgentBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class TableInfoServiceImpl extends ServiceImpl<TableInfoMapper, TableInfo> implements TableInfoService {

     private static final Gson GSON  =new Gson();

    @Override
    public void validAndHandleTableInfo(TableInfo tableInfo, boolean add) {
        if(tableInfo==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String content = tableInfo.getContent();
        Integer reviewStatus = tableInfo.getReviewStatus();
        String name = tableInfo.getName();
        if(add& StringUtils.isAnyBlank(name,content)){
         throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if(StringUtils.isNotBlank(name)&&name.length()>30){
          throw new BusinessException(ErrorCode.PARAMS_ERROR,"名字过长");
        }

        if(StringUtils.isNotBlank(content)){
            if(content.length()>20000){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"内容过长");
            }
            try{
                TableSchema tableSchema = GSON.fromJson(content, TableSchema.class);
                GeneratorFacade.validSchema(tableSchema);
            }catch (Exception ex){
                   throw new BusinessException(ErrorCode.PARAMS_ERROR,"内容错误");
            }

        }
        if(reviewStatus!=null&& !ReviewStatusEnum.getValues().contains(reviewStatus)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
    }
}
