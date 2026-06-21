package com.localjobfinder.bakend.exceptionPackage;

import com.localjobfinder.bakend.enums.ErrorMsg;
import com.localjobfinder.bakend.responsePackage.ApiResponse;
import com.localjobfinder.bakend.utilsPackage.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private String getPath(HttpServletRequest request) {
        return SecurityUtils.sanitize(request.getRequestURI());
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse> handleException(
                 CustomException ex, HttpServletRequest request ){

         ApiResponse apiResponse = new ApiResponse(
                 ex.getStatus(),
                 ex.getErrorCode().name(),
                 ex.getErrorCode().getMessage(),
                 getPath(request)
         );

         return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
        }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleException(
            Exception ex, HttpServletRequest request){

        ApiResponse apiResponse = new ApiResponse(
                500,
                ErrorMsg.GEN_001.name(),
                ErrorMsg.GEN_001.getMessage(),
                getPath(request)
        );

        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    // Handle Validation Errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationException(
            MethodArgumentNotValidException ex,HttpServletRequest request){

        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining(", "));

        ApiResponse apiResponse = new ApiResponse(
                HttpStatus.BAD_REQUEST.value(),
                ErrorMsg.VAL_003.name(),
                errorMessage,
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
    }

}
