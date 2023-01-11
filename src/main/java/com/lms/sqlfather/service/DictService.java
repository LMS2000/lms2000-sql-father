package com.lms.sqlfather.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lms.sqlfather.model.entity.Dict;
import com.lms.sqlfather.model.entity.User;

public interface DictService extends IService<Dict> {

    /**
     * 校验并处理
     *
     * @param dict
     * @param add 是否为创建校验
     */
    void validAndHandleDict(Dict dict, boolean add);

}
