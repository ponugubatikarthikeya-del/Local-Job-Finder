package com.localjobfinder.bakend.exceptionPackage.custom_exceptions;


import com.localjobfinder.bakend.exceptionPackage.ApplicationCustomException;
import org.springframework.http.HttpStatus;

public class DuplicateAvailableException extends ApplicationCustomException {

	public DuplicateAvailableException(String msg) {
		super(msg, HttpStatus.CONFLICT, "RESOURCE_ALREADY_EXISTS");
	}

}
