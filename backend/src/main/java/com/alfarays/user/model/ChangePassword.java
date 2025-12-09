package com.alfarays.user.model;

import com.alfarays.token.enums.TokenType;


public record ChangePassword(
        String currentPassword,
        String password,
        String confirmPassword,
        String token,
        TokenType type) {

    public boolean isValid() {
        return !currentPassword.equals(password) && password.equals(confirmPassword);
    }

}
