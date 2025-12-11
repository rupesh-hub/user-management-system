package com.alfarays.role.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RoleResponse(
        Long id,
        String role,
        String description,
        int totalUsers,
        boolean isSystemRole,
        LocalDateTime createdOn,
        LocalDateTime modifiedOn
) {
}
