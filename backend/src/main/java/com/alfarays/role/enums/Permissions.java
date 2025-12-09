package com.alfarays.role.enums;


import lombok.Getter;

@Getter
public enum Permissions {

    USER_MANAGEMENT("User Management"),
    ADMINISTRATION("Administration"),
    SYSTEM_MANAGEMENT("System Management"),
    CONTENT_MANAGEMENT("Content Management");

    private final String category;

    Permissions(String category) {
        this.category = category;
    }

}
