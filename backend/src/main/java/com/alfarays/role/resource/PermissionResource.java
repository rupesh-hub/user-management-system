package com.alfarays.role.resource;

import com.alfarays.role.enums.Permissions;
import com.alfarays.role.model.PermissionRequest;
import com.alfarays.role.model.PermissionResponse;
import com.alfarays.role.model.PermissionsResponse;
import com.alfarays.role.service.PermissionService;
import com.alfarays.util.GlobalResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionResource {

    private final PermissionService permissionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GlobalResponse<PermissionResponse> createPermission(@Valid @RequestBody PermissionRequest request) {
        return permissionService.create(request);
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public GlobalResponse<List<PermissionResponse>> createPermissions(
            @Valid @RequestBody List<PermissionRequest> requests
    ) {
        return permissionService.create(requests);
    }

    @GetMapping("/{id}")
    public GlobalResponse<PermissionResponse> getPermission(@PathVariable Long id) {
        return permissionService.byId(id);
    }

    @GetMapping
    public GlobalResponse<List<PermissionResponse>> getPermissions(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return permissionService.getAll(page, size);
    }

    @PutMapping("/{id}")
    public GlobalResponse<PermissionResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PermissionRequest request
    ) {
        return permissionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePermission(@PathVariable Long id) {
        permissionService.delete(id);
    }

    @GetMapping("/by.category/{category}")
    public List<PermissionResponse> byCategories(@PathVariable Permissions category) {
        return permissionService.byCategory(category);
    }

    @GetMapping("/by.role/{roleId}")
    public List<PermissionResponse> getPermissionsByRole(@PathVariable Long roleId) {
        return permissionService.byRole(roleId);
    }

    @GetMapping("/by.categories")
    public GlobalResponse<List<PermissionsResponse>> getPermissionsByCategory() {
        return permissionService.byCategories();
    }

}
