package com.localjobfinder.bakend.exceptionPackage;

import com.localjobfinder.bakend.enums.ErrorMsg;
import com.localjobfinder.bakend.responsePackage.AuthResponse;
import com.localjobfinder.bakend.utilsPackage.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {


    private String getPath(HttpServletRequest request) {
        return SecurityUtils.sanitize(request.getRequestURI());
    }

    @ExceptionHandler(ApplicationCustomException.class)
    public ResponseEntity<ApiErrorResponse> handleApplicationCustomException(
            ApplicationCustomException ex, HttpServletRequest request) {

        String path = getPath(request);

        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(ex.getHttpStatus().value())
                .errorCode(ex.getErrorCode())
                .errorMessage(ex.getMessage())
                .path(path)
                .build();

        return new ResponseEntity<>(response,ex.getHttpStatus());

    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception ex, HttpServletRequest request) {

        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .errorCode("INTERNAL_SERVER_ERROR")
                .errorMessage(ex.getMessage())
//                .errorMessage("an unexcepted error occurs.please contact support")
                .path(getPath(request))
                .build();

        return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {

        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode("BAD_REQUEST")
                .errorMessage(ex.getMessage())
                .path(getPath(request))
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiErrorResponse> handleIOException(
            IOException ex, HttpServletRequest request ) {

        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode("BAD_REQUEST")
                .errorMessage(ex.getMessage())
                .path(getPath(request))
                .build();

        return ResponseEntity.badRequest().body(response);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request ) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            if (error instanceof FieldError fieldError) { // Safe pattern matching (Java 16+)
                errors.put(fieldError.getField(), fieldError.getDefaultMessage());
            } else {
                // Handles global class-level errors cleanly without crashing
                errors.put(error.getObjectName(), error.getDefaultMessage());
            }
        });

        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode("BAD_REQUEST")
                .errorMessage("Input validation failed")
                .validationErrors(errors)
                .path(getPath(request))
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


    // user send extra fields as in request
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnknownFields(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String errorMessage = "Malformed JSON request or unrecognized fields provided.";

        // Extract exact field name if caused by UnrecognizedPropertyException
        if (ex.getCause() instanceof UnrecognizedPropertyException unrecognizedEx) {
            errorMessage = "Unrecognized field '" + unrecognizedEx.getPropertyName() + "' is not allowed.";
        }

        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode("BAD_REQUEST")
                .errorMessage(errorMessage)
                .path(getPath(request))
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

    }

    // for params validation
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodValidationException(
            HandlerMethodValidationException exception,
            HttpServletRequest request
    ) {
        Map<String, String> validationErrors = new LinkedHashMap<>();

        exception.getParameterValidationResults().forEach(result -> {
            String paramName = result.getMethodParameter().getParameterName();
            result.getResolvableErrors().forEach(error ->
                    validationErrors.merge(
                            paramName != null ? paramName : "request",
                            Objects.requireNonNull(error.getDefaultMessage()),
                            (existing, incoming) -> existing + ", " + incoming));
        });

        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode("VALIDATION_ERROR")
                .errorMessage("Request validation failed.")
                .path(getPath(request))
                .validationErrors(validationErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

}
