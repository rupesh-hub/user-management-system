package com.alfarays.role.model;

import com.alfarays.role.entity.Permission;
import com.alfarays.role.entity.Role;

import java.util.Set;
import java.util.stream.Collectors;

public record PermissionResponse(
        Long id,
        String permission,
        String category,
        String description,
        Set<String> roles
) {

    public static PermissionResponse fromEntity(Permission permission) {
        return new PermissionResponse(
                permission.getId(),
                permission.getPermission().toLowerCase(),
                permission.getCategory().getCategory().toLowerCase(),
                permission.getDescription(),
                permission.getRoles().stream()
                        .map(Role::getRole)
                        .collect(Collectors.toSet())
        );
    }

}
