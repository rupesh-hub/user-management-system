package com.alfarays.user.model;

import com.alfarays.image.model.ImageResponse;
import com.alfarays.role.entity.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private String userId;
    private String firstname;
    private String lastname;
    private String username;
    private String email;
    private List<Role> roles;
    private boolean enabled;
    private ImageResponse profile;
    private LocalDateTime createdOn;
    private LocalDateTime modifiedOn;
    private String createdBy;
    private String modifiedBy;

}