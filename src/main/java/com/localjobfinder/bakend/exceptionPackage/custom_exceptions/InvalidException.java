package com.localjobfinder.bakend.exceptionPackage.custom_exceptions;

import com.localjobfinder.bakend.exceptionPackage.ApplicationCustomException;
import org.springframework.http.HttpStatus;


public class InvalidException extends ApplicationCustomException {

    public InvalidException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "INVALID_REQUEST");
    }

}
