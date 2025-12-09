package com.alfarays.user.mapper;


import com.alfarays.authentication.model.RegistrationRequest;
import com.alfarays.image.entity.Image;
import com.alfarays.image.model.ImageResponse;
import com.alfarays.user.entity.User;
import com.alfarays.user.model.UserResponse;

import java.util.Optional;
import java.util.UUID;

public final class UserMapper {

    private UserMapper() {
    }

    public static User toEntity(RegistrationRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFirstname(request.firstname());
        user.setLastname(request.lastname());
        user.setPassword(request.password());
        return user;
    }


    public static UserResponse toResponse(User user) {
        return UserResponse
                .builder()
                .userId(UUID.randomUUID().toString())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .username(user.getUsername())
                .enabled(user.getEnabled())
                .roles(user.getRoles())
                .profile(
                        Optional.ofNullable(user.getProfile())
                                .map(UserMapper::imageResponse)
                                .orElse(null)
                )
                .createdOn(user.getCreatedOn())
                .modifiedOn(user.getModifiedOn())
                .build();
    }

    private static String randomString() {
        return UUID.randomUUID().toString();
    }

    public static ImageResponse imageResponse(Image image) {
        return ImageResponse
                .builder()
                .id(image.getId())
                .filename(image.getFilename())
                .type(image.getType())
                .size(image.getSize())
                .createdOn(image.getCreatedOn().toString())
                .modifiedOn(image.getModifiedOn().toString())
                .modifiedBy(image.getModifiedBy())
                .path(image.getPath())
                .build();
    }


}
