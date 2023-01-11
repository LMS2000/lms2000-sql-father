package com.lms.sqlfather.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lms.sqlfather.core.schema.TableSchema;
import com.lms.sqlfather.model.entity.FieldInfo;
import com.lms.sqlfather.model.entity.User;

public interface FieldInfoService extends IService<FieldInfo> {

    /**
     * 校验并处理
     *
     * @param fieldInfo
     * @param add 是否为创建校验
     */
    void validAndHandleFieldInfo(FieldInfo fieldInfo, boolean add);
}
