package com.localjobfinder.bakend.exceptionPackage.custom_exceptions;

import com.localjobfinder.bakend.exceptionPackage.ApplicationCustomException;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException  extends ApplicationCustomException {

    public ResourceNotFoundException(String msg) {
        super(msg, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }

}
