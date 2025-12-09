package com.alfarays.role.entity;


import com.alfarays.role.enums.Permissions;
import com.alfarays.shared.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "_permissions")
public class Permission extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "_permission_id_seq_generator")
    @SequenceGenerator(name = "_permission_id_seq_generator", sequenceName = "_permission_id_seq", allocationSize = 1, initialValue = 1)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;

    @Column(name = "permission", nullable = false, unique = true)
    private String permission;

    private Permissions category;
    private String description;

    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<Role> roles = new HashSet<>();

}
