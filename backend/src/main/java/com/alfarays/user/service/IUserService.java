package com.alfarays.user.service;

import com.alfarays.authentication.model.RegistrationRequest;
import com.alfarays.user.model.ChangePassword;
import com.alfarays.user.model.UserFilterDTO;
import com.alfarays.user.model.UserResponse;
import com.alfarays.util.GlobalResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IUserService {

    GlobalResponse<UserResponse> byUsername(String username);

    GlobalResponse<UserResponse> byEmail(String email);

    GlobalResponse<UserResponse> byId(Long id);

    GlobalResponse<Boolean> update(RegistrationRequest request, String username, MultipartFile profile) throws IOException;

    GlobalResponse<Void> changeProfilePicture(MultipartFile profile, Authentication authentication);

    GlobalResponse<Boolean> delete(String userId) throws IOException;

    GlobalResponse<Boolean> assignRole(String username, String[] names);

    GlobalResponse<Boolean> removeRole(String username, String[] names);

    GlobalResponse<Void> resetPasswordRequest(String username);

    GlobalResponse<Void> changePassword(ChangePassword request, Authentication authentication);

    GlobalResponse<List<UserResponse>> getFilteredUsers(UserFilterDTO filter, Pageable pageable);

}
