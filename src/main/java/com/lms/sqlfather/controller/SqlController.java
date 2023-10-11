package com.lms.sqlfather.controller;


import com.lms.result.EnableResponseAdvice;
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
@EnableResponseAdvice
public class SqlController {

    @PostMapping("/generate/schema")
    public GenerateVO generateBySchema(@RequestBody TableSchema tableSchema) {
        return GeneratorFacade.generateAll(tableSchema);
    }

    @PostMapping("/get/schema/auto")
    public TableSchema getSchemaByAuto(@RequestBody GenerateByAutoRequest autoRequest) {

        BusinessException.throwIf(autoRequest == null);
        return TableSchemaBuilder.buildFromAuto(autoRequest.getContent());
    }
    /**
     * 根据 SQL 获取 schema
     *
     * @param sqlRequest
     * @return
     */
    @PostMapping("/get/schema/sql")
    public TableSchema getSchemaBySql(@RequestBody GenerateBySqlRequest sqlRequest) {

        BusinessException.throwIf(sqlRequest == null);
        // 获取 tableSchema
        return TableSchemaBuilder.buildFromSql(sqlRequest.getSql());
    }

    @PostMapping("/get/schema/excel")
    public TableSchema getSchemaByExcel(MultipartFile file) {
        return TableSchemaBuilder.buildFromExcel(file);
    }


}
