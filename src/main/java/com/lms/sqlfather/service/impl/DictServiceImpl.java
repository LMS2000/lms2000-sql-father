package com.lms.sqlfather.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.lms.sqlfather.common.ErrorCode;
import com.lms.sqlfather.exception.BusinessException;
import com.lms.sqlfather.mapper.DictMapper;
import com.lms.sqlfather.mapper.UserMapper;
import com.lms.sqlfather.model.entity.Dict;
import com.lms.sqlfather.model.entity.User;
import com.lms.sqlfather.model.enums.ReviewStatusEnum;
import com.lms.sqlfather.service.DictService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.awt.image.BufferStrategy;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict>  implements DictService {
    private final static Gson GSON = new Gson();
    @Override
    public void validAndHandleDict(Dict dict, boolean add) {
       if(dict==null){
           throw new BusinessException(ErrorCode.PARAMS_ERROR);
       }
        String content = dict.getContent();
        String name = dict.getName();
        Integer reviewStatus = dict.getReviewStatus();
        if(add&& StringUtils.isAnyBlank(name,content)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isNotBlank(name) && name.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "名称过长");
        }
        if(StringUtils.isNotBlank(content)){

            if(content.length()>20000){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"内容过长");
            }


            try{
                String[] words = content.split("[,.]");
                for (int i = 0; i < words.length; i++) {
                    words[i]=words[i].trim();
                }
                List<String> collect = Arrays.stream(words)
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toList());
                dict.setContent(GSON.toJson(collect));
            }catch (Exception ex){
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }
        if (reviewStatus != null && !ReviewStatusEnum.getValues().contains(reviewStatus)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
    }
}
