package com.alfarays.authentication.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Builder
public class AuthenticationResponse {
    private String name;
    private String profile;
    private String email;
    private String username;
    @JsonProperty("access_token")
    private String token;
    private Set<String> roles;
}
