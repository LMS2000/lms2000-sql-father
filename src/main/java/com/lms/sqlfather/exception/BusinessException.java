package com.lms.sqlfather.exception;


import com.lms.contants.HttpCode;

/**
 * 自定义异常类
 *
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(HttpCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(HttpCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
    public static void throwIfOperationAdmin(Integer uid){
        throwIf(uid==1);
    }

    public static void throwIf(boolean flag){
        if(flag)throw new BusinessException(HttpCode.OPERATION_ERROR);
    }
    public static  void throwIfNot(boolean flag){
        if(!flag)throw new BusinessException(HttpCode.OPERATION_ERROR);
    }
    public static void throwIf(boolean flag,HttpCode httpCode){
        if(flag)throw new BusinessException(httpCode);
    }
    public static void throwIf(boolean flag,String message){
        if(flag)throw new BusinessException(HttpCode.PARAMS_ERROR,message);
    }
    public static void throwIfNot(boolean flag,String message){
        if(flag)throw new BusinessException(HttpCode.PARAMS_ERROR,message);
    }
    public static  void throwIfNot(boolean flag,HttpCode httpCode){
        if(!flag)throw new BusinessException(httpCode);
    }

    public static void throwIf(boolean flag,HttpCode httpCode,String message){
        if(flag)throw new BusinessException(httpCode,message);
    }
    public static  void throwIfNot(boolean flag,HttpCode httpCode,String message){
        if(!flag)throw new BusinessException(httpCode,message);
    }
    public int getCode() {
        return code;
    }
}
