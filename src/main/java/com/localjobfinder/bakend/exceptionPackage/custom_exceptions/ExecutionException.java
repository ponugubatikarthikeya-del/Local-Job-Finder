package com.localjobfinder.bakend.exceptionPackage.custom_exceptions;


import com.localjobfinder.bakend.exceptionPackage.ApplicationCustomException;
import org.springframework.http.HttpStatus;

public class ExecutionException extends ApplicationCustomException {

    public ExecutionException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR,"FIREBASE_ERROR");
    }

}
