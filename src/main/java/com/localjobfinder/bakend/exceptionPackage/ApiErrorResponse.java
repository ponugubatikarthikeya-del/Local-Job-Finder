package com.localjobfinder.bakend.exceptionPackage;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    private final Instant timestamp;
    private final int status;
    private final String errorCode;
    private final String errorMessage;
    private final String path;
    private final Map<String, String> validationErrors;

}
