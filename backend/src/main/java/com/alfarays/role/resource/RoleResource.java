package com.alfarays.role.resource;

import com.alfarays.role.model.RoleRequest;
import com.alfarays.role.model.RoleResponse;
import com.alfarays.role.service.IRoleService;
import com.alfarays.util.GlobalResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleResource {

    private final IRoleService roleService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<GlobalResponse<RoleResponse>> create(@RequestBody @Valid RoleRequest request) {
        return ResponseEntity.ok(roleService.create(request));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<GlobalResponse<RoleResponse>> update(
            @PathVariable long id,
            @RequestBody @Valid RoleRequest request) {
        return ResponseEntity.ok(roleService.update(id, request));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GlobalResponse<List<RoleResponse>>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(roleService.getAll(page, size));
    }

    @GetMapping("/by.name/{name}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GlobalResponse<RoleResponse>> getByName(@PathVariable String name) {
        return ResponseEntity.ok(roleService.getByName(name));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<GlobalResponse<Void>> delete(@PathVariable long id) {
        return ResponseEntity.ok(roleService.delete(id));
    }

}
