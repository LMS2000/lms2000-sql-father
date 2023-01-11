package com.lms.sqlfather.controller;

import com.lms.sqlfather.common.BaseResponse;
import com.lms.sqlfather.common.ErrorCode;
import com.lms.sqlfather.common.ResultUtils;
import com.lms.sqlfather.core.GeneratorFacade;
import com.lms.sqlfather.core.model.vo.GenerateVO;
import com.lms.sqlfather.core.schema.TableSchema;
import com.lms.sqlfather.core.schema.TableSchemaBuilder;
import com.lms.sqlfather.exception.BusinessException;
import com.lms.sqlfather.model.dto.GenerateByAutoRequest;
import com.lms.sqlfather.model.dto.GenerateBySqlRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/sql")
public class SqlController {

    @PostMapping("/generate/schema")
    public BaseResponse<GenerateVO> generateBySchema(@RequestBody TableSchema tableSchema) {
        return ResultUtils.success(GeneratorFacade.generateAll(tableSchema));
    }

    @PostMapping("/get/schema/auto")
    public BaseResponse<TableSchema> getSchemaByAuto(@RequestBody GenerateByAutoRequest autoRequest) {
        if (autoRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(TableSchemaBuilder.buildFromAuto(autoRequest.getContent()));
    }
    /**
     * 根据 SQL 获取 schema
     *
     * @param sqlRequest
     * @return
     */
    @PostMapping("/get/schema/sql")
    public BaseResponse<TableSchema> getSchemaBySql(@RequestBody GenerateBySqlRequest sqlRequest) {
        if (sqlRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取 tableSchema
        return ResultUtils.success(TableSchemaBuilder.buildFromSql(sqlRequest.getSql()));
    }

    @PostMapping("/get/schema/excel")
    public BaseResponse<TableSchema> getSchemaByExcel(MultipartFile file) {
        return ResultUtils.success(TableSchemaBuilder.buildFromExcel(file));
    }


}
