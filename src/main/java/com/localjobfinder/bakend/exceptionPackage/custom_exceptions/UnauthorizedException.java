package com.localjobfinder.bakend.exceptionPackage.custom_exceptions;

import com.localjobfinder.bakend.exceptionPackage.ApplicationCustomException;
import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ApplicationCustomException {

    public UnauthorizedException(String msg) {
        super(msg, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED_ACCESS");
    }

}
