package com.lms.sqlfather.exception;


import com.lms.contants.HttpCode;
import com.lms.result.ResultData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends com.lms.exception.GlobalExceptionHandler {


    /**
     * 控制返回的状态码
     * @param businessException
     * @return
     */
    @ExceptionHandler(BusinessException.class)
    public ResultData AuthException(BusinessException businessException){
        log.error("进入自定义异常："+businessException.getCode());

        HttpCode[] values = HttpCode.values();
        for (HttpCode httpCode : values) {
            if(httpCode.getCode()==businessException.getCode()){
                return ResultData.error(httpCode,
                        businessException.getMessage(),null);
            }
        }
        return ResultData.error(businessException.getMessage());
    }


}
