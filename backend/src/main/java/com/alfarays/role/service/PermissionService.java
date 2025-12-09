package com.alfarays.role.service;

import com.alfarays.exception.AuthorizationException;
import com.alfarays.role.entity.Permission;
import com.alfarays.role.entity.Role;
import com.alfarays.role.enums.Permissions;
import com.alfarays.role.model.PermissionRequest;
import com.alfarays.role.model.PermissionResponse;
import com.alfarays.role.model.PermissionsResponse;
import com.alfarays.role.repository.PermissionRepository;
import com.alfarays.role.repository.RoleRepository;
import com.alfarays.util.GlobalResponse;
import com.alfarays.util.Paging;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public GlobalResponse<PermissionResponse> create(PermissionRequest request) {
        if(permissionRepository.existsByPermission(request.permission())) {
            throw new AuthorizationException("Permission with name " + request.permission() + " already exists");
        }
        Permission permission = new Permission();
        permission.setPermission(request.permission().toLowerCase());
        permission.setCategory(request.category());
        permission.setDescription(request.description());
        permission = permissionRepository.save(permission);
        log.info("Created new permission with ID: {}", permission.getId());
        return GlobalResponse.success(PermissionResponse.fromEntity(permission));
    }


    public GlobalResponse<List<PermissionResponse>> create(List<PermissionRequest> requests) {
        List<String> permissionNames = requests.stream()
                .map(PermissionRequest::permission)
                .map(String::toLowerCase)
                .toList();

        List<String> existingPermissions = permissionRepository.findByPermissionIn(permissionNames)
                .stream()
                .map(Permission::getPermission)
                .toList();

        if(!existingPermissions.isEmpty()) {
            throw new AuthorizationException(
                    "Some permissions already exist: " + String.join(", ", existingPermissions)
            );
        }

        // Create all permissions
        List<Permission> createdPermissions = requests.stream()
                .map(request -> {
                    Permission p = new Permission();
                    p.setPermission(request.permission().toLowerCase());
                    p.setCategory(request.category());
                    p.setDescription(request.description());
                    return p;
                })
                .toList();

        List<Permission> savedPermissions = permissionRepository.saveAll(createdPermissions);
        log.info("Created {} new permissions.", savedPermissions.size());

        List<PermissionResponse> responses = savedPermissions.stream()
                .map(PermissionResponse::fromEntity)
                .toList();

        return GlobalResponse.success(responses);
    }

    public GlobalResponse<PermissionResponse> byId(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new AuthorizationException("Permission not found with ID: " + id));
        return GlobalResponse.success(PermissionResponse.fromEntity(permission));
    }

    public GlobalResponse<List<PermissionResponse>> getAll(int page, int limit) {
        Page<Permission> permissionPage = permissionRepository.findAll(PageRequest.of(page, limit));
        return GlobalResponse.success(
                permissionPage.getContent()
                        .stream()
                        .map(PermissionResponse::fromEntity).collect(Collectors.toList())
                ,
                Paging.builder()
                        .page(page)
                        .size(limit)
                        .totalElements(permissionPage.getTotalElements())
                        .totalPages(permissionPage.getTotalPages())
                        .isFirst(permissionPage.isFirst())
                        .isLast(permissionPage.isLast())
                        .build()
        );
    }

    public GlobalResponse<PermissionResponse> update(Long id, PermissionRequest request) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new AuthorizationException("Permission not exists with ID: " + id));

        if(!permission.getPermission().equals(request.permission()) && permissionRepository.existsByPermission(request.permission())) {
            throw new AuthorizationException("Permission with name " + request.permission() + " already exists.");
        }

        permission.setPermission(request.permission());
        permission.setCategory(request.category());
        permission.setDescription(request.description());

        permission = permissionRepository.save(permission);
        log.info("Updated permission with ID: {}", permission.getId());
        return GlobalResponse.success(PermissionResponse.fromEntity(permission));
    }

    public void delete(Long id) {
        if(!permissionRepository.existsById(id))
            throw new AuthorizationException("Permission not found with ID: " + id);
        if(roleRepository.existsByPermissionsId(id))
            throw new AuthorizationException("Cannot delete permission as it's assigned to one or more roles");
        permissionRepository.deleteById(id);
        log.info("Deleted permission with ID: {}", id);
    }

    public List<PermissionResponse> byRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AuthorizationException("Role not found with ID: " + roleId));

        return role.getPermissions().stream()
                .map(PermissionResponse::fromEntity)
                .toList();
    }

    public List<PermissionResponse> byCategory(Permissions category) {
        return permissionRepository.findByCategory(category).stream()
                .map(PermissionResponse::fromEntity)
                .toList();
    }

    public GlobalResponse<List<PermissionsResponse>> byCategories() {

        Map<String, List<PermissionResponse>> grouped = permissionRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        p -> p.getCategory().getCategory(),
                        Collectors.mapping(PermissionResponse::fromEntity, Collectors.toList())
                ));

        List<PermissionsResponse> result = grouped.entrySet().stream()
                .map(e -> new PermissionsResponse(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return GlobalResponse.success(result);
    }


}
