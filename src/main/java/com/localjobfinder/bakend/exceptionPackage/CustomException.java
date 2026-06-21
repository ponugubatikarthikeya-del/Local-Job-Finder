package com.localjobfinder.bakend.exceptionPackage;

import com.localjobfinder.bakend.enums.ErrorMsg;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ErrorMsg errorCode;
    private final int status;

    public CustomException(ErrorMsg errorCode, int status) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.status = status;
    }

    public CustomException(ErrorMsg errorCode, int status,String customMsg) {
        super(customMsg);
        this.errorCode = errorCode;
        this.status = status;
    }


}
