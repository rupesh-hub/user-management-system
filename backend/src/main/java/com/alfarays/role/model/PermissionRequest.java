package com.alfarays.role.model;

import com.alfarays.role.enums.Permissions;
import jakarta.validation.constraints.NotBlank;

public record PermissionRequest(
        @NotBlank String permission,
        Permissions category,
        String description
) {
}
