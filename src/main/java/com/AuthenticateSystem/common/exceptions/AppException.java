package com.AuthenticateSystem.common.exceptions;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private  final  ErrorCode errorCode;

    public  AppException(ErrorCode errorCode){
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public AppException(ErrorCode errorCode, String message){
        super(message);
        this.errorCode = errorCode;
    }

    public  AppException(ErrorCode errorCode, Throwable cause){
        super(errorCode.getDefaultMessage(), cause);
        this.errorCode = errorCode;
    }
}
