package com.alfarays.role.service;

import com.alfarays.role.model.RoleRequest;
import com.alfarays.role.model.RoleResponse;
import com.alfarays.util.GlobalResponse;

import java.util.List;

public interface IRoleService {
    GlobalResponse<RoleResponse> create(RoleRequest request);

    GlobalResponse<List<RoleResponse>> getAll(int page, int size);

    GlobalResponse<RoleResponse> getByName(String name);

    GlobalResponse<RoleResponse> update(Long id, RoleRequest request);

    GlobalResponse<Void> delete(long id);
}
