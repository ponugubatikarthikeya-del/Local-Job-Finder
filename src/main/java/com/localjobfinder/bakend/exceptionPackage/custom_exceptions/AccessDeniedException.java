package com.localjobfinder.bakend.exceptionPackage.custom_exceptions;

import com.localjobfinder.bakend.exceptionPackage.ApplicationCustomException;
import org.springframework.http.HttpStatus;

public class AccessDeniedException extends ApplicationCustomException {

    public AccessDeniedException(String msg) {
        super(msg, HttpStatus.FORBIDDEN,"ACCESS_DENIED");
    }

}
