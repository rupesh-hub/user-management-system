package com.alfarays.authentication.service;

import com.alfarays.authentication.model.AuthenticationRequest;
import com.alfarays.authentication.model.AuthenticationResponse;
import com.alfarays.authentication.model.ForgetPasswordRequest;
import com.alfarays.authentication.model.RegistrationRequest;
import com.alfarays.token.enums.TokenType;
import com.alfarays.util.GlobalResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MultipartFile;

public interface IAuthenticationService {
    GlobalResponse<Void> register(RegistrationRequest request, MultipartFile image) throws MethodArgumentNotValidException;
    GlobalResponse<Void> forgetPasswordRequest(String username);
    GlobalResponse<Void> changePassword(ForgetPasswordRequest request);
    GlobalResponse<Void> resendConfirmationToken(String username);
    GlobalResponse<Void> activateAccount(String username, String value, TokenType type);
    GlobalResponse<Boolean> existsByEmail(String email);
    GlobalResponse<Boolean> existsByUsername(String username);

}
