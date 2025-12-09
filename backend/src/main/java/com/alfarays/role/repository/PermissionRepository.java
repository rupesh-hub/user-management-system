package com.alfarays.role.repository;

import com.alfarays.role.entity.Permission;
import com.alfarays.role.enums.Permissions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByPermission(String permission);

    boolean existsByPermission(String permission);

    List<Permission> findByCategory(Permissions category);

    List<Permission> findByPermissionIn(List<String> permissions);
}
