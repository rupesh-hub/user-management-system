package com.alfarays.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GlobalResponse<T> {

    String message;
    ResponseStatus status;
    String code;
    String error;
    List<String> errors;
    T data;
    Paging page;

    public enum ResponseStatus {
        SUCCESS, FAILURE
    }

    public static <T> GlobalResponse<T> success(T data) {
        return GlobalResponse.<T>builder()
                .message("Success")
                .status(ResponseStatus.SUCCESS)
                .code("200")
                .data(data)
                .build();
    }

    public static <T> GlobalResponse<T> success(T data, Paging page) {
        return GlobalResponse.<T>builder()
                .message("Success")
                .status(ResponseStatus.SUCCESS)
                .code("200")
                .data(data)
                .page(page)
                .build();
    }

    public static <T> GlobalResponse<T> success(String message, T data) {
        return GlobalResponse.<T>builder()
                .message(message)
                .status(ResponseStatus.SUCCESS)
                .code("200")
                .data(data)
                .build();
    }

    public static GlobalResponse<Void> success(String message) {
        return GlobalResponse.<Void>builder()
                .message(message)
                .status(ResponseStatus.SUCCESS)
                .code("200")
                .build();
    }

    public static GlobalResponse<Void> success() {
        return GlobalResponse.<Void>builder()
                .status(ResponseStatus.SUCCESS)
                .code("200")
                .build();
    }

    public static GlobalResponse<Void> failure(String errorMessage) {
        return GlobalResponse.<Void>builder()
                .status(ResponseStatus.FAILURE)
                .code("500")
                .error(errorMessage)
                .build();
    }

    public static GlobalResponse<Void> failure(List<String> errors) {
        return GlobalResponse.<Void>builder()
                .status(ResponseStatus.FAILURE)
                .code("500")
                .errors(errors)
                .build();
    }

    public static GlobalResponse<Void> failure(String errorMessage, String code) {
        return GlobalResponse.<Void>builder()
                .status(ResponseStatus.FAILURE)
                .code(code)
                .error(errorMessage)
                .build();
    }

}
