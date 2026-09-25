package com.localjobfinder.bakend.paging;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.Instant;

@Getter
public class JobPagingResponse<T> {

    private final Instant timestamp;
    private final int status;
    private final String errorCode;
    private final String errorMessage;
    private final String path;

    private final T data;

    @JsonCreator
    public JobPagingResponse(
            @JsonProperty("timestamp") Instant timestamp,
            @JsonProperty("status") int status,
            @JsonProperty("errorCode") String errorCode,
            @JsonProperty("errorMessage") String errorMessage,
            @JsonProperty("path") String path,
            @JsonProperty("data") T data) {
        this.timestamp = timestamp;
        this.status = status;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.path = path;
        this.data = data;
    }

    public JobPagingResponse(int status, String errorCode, String errorMessage, String path, T data) {
        this(Instant.now(), status, errorCode, errorMessage, path, data);
    }
}

