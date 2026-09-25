package com.localjobfinder.bakend.exceptionPackage;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ApplicationCustomException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String ErrorCode;

    protected ApplicationCustomException(String msg,HttpStatus httpStatus, String ErrorCode){
        super(msg);
        this.httpStatus = httpStatus;
        this.ErrorCode = ErrorCode;
    }

}
