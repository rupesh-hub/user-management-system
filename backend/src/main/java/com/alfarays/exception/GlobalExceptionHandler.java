package com.alfarays.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorMessage> handleException(LockedException exp) {
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ErrorMessage.builder()
                                .code("401")
                                .message("Your account is locked! Please contact admin.")
                                .build()
                );
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorMessage> handleException(DisabledException exp) {
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ErrorMessage.builder()
                                .code("401")
                                .message("Your account is disabled! Please contact admin.")
                                .build()
                );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorMessage> handleException(HttpServletRequest servletRequest) {
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ErrorMessage.builder()
                                .timestamp(LocalDateTime.now())
                                .code("400")
                                .message("Something went wrong!")
                                .status(BAD_REQUEST)
                                .uri(servletRequest.getRequestURI())
                                .build()
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exp, HttpServletRequest servletRequest) {

        Map<String, String> errors = new HashMap<>();

        exp.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    if(error instanceof FieldError fieldError)
                        errors.put(fieldError.getField(), error.getDefaultMessage());
                    else
                        errors.put(error.getObjectName(), error.getDefaultMessage());
                });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ErrorMessage.builder()
                                .timestamp(LocalDateTime.now())
                                .code("400")
                                .message("Validation failed!")
                                .status(HttpStatus.BAD_REQUEST)
                                .errors(errors)
                                .uri(servletRequest.getRequestURI())
                                .build()
                );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorMessage> handleAuthenticationException(AuthenticationException exp, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        ErrorMessage.builder()
                                .timestamp(LocalDateTime.now())
                                .code("401")
                                .message("You are not authenticated. Please log in to access this resource.")
                                .status(UNAUTHORIZED)
                                .uri(request.getRequestURI())
                                .build()
                );
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleException(Exception exp, HttpServletRequest request) {
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(
                        ErrorMessage.builder()
                                .message(exp.getLocalizedMessage())
                                .code("500")
                                .timestamp(LocalDateTime.now())
                                .status(INTERNAL_SERVER_ERROR)
                                .uri(request.getRequestURI())
                                .build()
                );
    }


}
