package com.localjobfinder.bakend.exceptionPackage;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.localjobfinder.bakend.enums.ErrorMsg;
import com.localjobfinder.bakend.responsePackage.AuthResponse;
import com.localjobfinder.bakend.utilsPackage.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private String getPath(HttpServletRequest request) {
        return SecurityUtils.sanitize(request.getRequestURI());
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<AuthResponse> handleException(
                 CustomException ex, HttpServletRequest request ){

         String message = ex.getMessage() != null && !ex.getMessage().isBlank() ? ex.getMessage() : ex.getErrorCode().getMessage();

         AuthResponse authResponse = new AuthResponse(
                 ex.getStatus(),
                 ex.getErrorCode().name(),
                 message,
                 getPath(request)
         );

         return ResponseEntity.status(authResponse.getStatus()).body(authResponse);
        }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<AuthResponse> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request){

        AuthResponse authResponse = new AuthResponse(
                401,
                ErrorMsg.AUTH_004.name(),
                ex.getMessage(),
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(authResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<AuthResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request){

        AuthResponse authResponse = new AuthResponse(
                403,
                ErrorMsg.GEN_001.name(),
                "Access denied",
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(authResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AuthResponse> handleException(
            Exception ex, HttpServletRequest request){

        AuthResponse authResponse = new AuthResponse(
                500,
                ErrorMsg.GEN_001.name(),
                ErrorMsg.GEN_001.getMessage(),
                getPath(request)
        );

        return ResponseEntity.status(authResponse.getStatus()).body(authResponse);
    }

    // Handle Validation Errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AuthResponse> handleValidationException(
            MethodArgumentNotValidException ex,HttpServletRequest request){

//        fieldError.getField() + ": "
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining(", "));

        AuthResponse authResponse = new AuthResponse(
                HttpStatus.BAD_REQUEST.value(),
                ErrorMsg.VAL_003.name(),
                errorMessage,
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(authResponse);
    }

    // Handle Firebase errors
    @ExceptionHandler(ExecutionException.class)
    public ResponseEntity<AuthResponse> handleFirebaseException(
            ExecutionException ex,
            HttpServletRequest request) {

        String safePath = SecurityUtils.sanitize(request.getRequestURI());

        AuthResponse response = new AuthResponse(
                500,
                "FIREBASE_ERROR",
                "Firebase operation failed",
                safePath
        );

        return ResponseEntity.status(500).body(response);
    }


    // user send extra fields as in request
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<AuthResponse> handleUnknownFields(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String errorMessage = "Malformed JSON request or unrecognized fields provided.";

        // Extract exact field name if caused by UnrecognizedPropertyException
        if (ex.getCause() instanceof UnrecognizedPropertyException unrecognizedEx) {
            errorMessage = "Unrecognized field '" + unrecognizedEx.getPropertyName() + "' is not allowed.";
        }

        String safePath = SecurityUtils.sanitize(request.getRequestURI());

        AuthResponse error = new AuthResponse(
                400,
                ErrorMsg.VAL_004.name(),
                errorMessage,
                safePath
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

}
