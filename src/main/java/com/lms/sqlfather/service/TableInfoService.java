package com.lms.sqlfather.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lms.sqlfather.model.entity.TableInfo;
import com.lms.sqlfather.model.entity.User;

public interface TableInfoService extends IService<TableInfo> {


    /**
     * 校验并处理
     *
     * @param tableInfo
     * @param add 是否为创建校验
     */
    void validAndHandleTableInfo(TableInfo tableInfo, boolean add);
}
