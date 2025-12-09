package com.alfarays.role.model;

import java.util.List;

public record PermissionsResponse(String category, List<PermissionResponse> permissions) {
}
