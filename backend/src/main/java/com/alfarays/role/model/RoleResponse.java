package com.alfarays.role.model;

public record RoleResponse(
        Long id,
        String role,
        String description,
        int totalUsers,
        boolean isSystemRole,
        String createdOn,
        String modifiedOn
) {
}
