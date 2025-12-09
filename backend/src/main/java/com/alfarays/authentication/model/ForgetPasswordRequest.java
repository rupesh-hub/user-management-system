package com.alfarays.authentication.model;

public record ForgetPasswordRequest(
        String username,
        String password,
        String confirmPassword,
        String token
) {
}
