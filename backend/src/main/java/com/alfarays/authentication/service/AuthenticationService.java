package com.alfarays.authentication.service;

import com.alfarays.authentication.model.AuthenticationRequest;
import com.alfarays.authentication.model.AuthenticationResponse;
import com.alfarays.authentication.model.ForgetPasswordRequest;
import com.alfarays.authentication.model.RegistrationRequest;
import com.alfarays.exception.AuthorizationException;
import com.alfarays.image.entity.Image;
import com.alfarays.image.service.ImageService;
import com.alfarays.mail.enums.MailSubject;
import com.alfarays.mail.enums.MailTemplate;
import com.alfarays.mail.model.MailRequest;
import com.alfarays.mail.service.IMailService;
import com.alfarays.role.repository.RoleRepository;
import com.alfarays.token.enums.DurationUnit;
import com.alfarays.token.enums.TokenType;
import com.alfarays.token.service.ITokenService;
import com.alfarays.user.entity.User;
import com.alfarays.user.mapper.UserMapper;
import com.alfarays.user.repository.UserRepository;
import com.alfarays.util.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.alfarays.mail.enums.MailSubject.ACCOUNT_ACTIVATION_REQUEST;
import static com.alfarays.mail.enums.MailTemplate.FORGOT_PASSWORD_REQUEST;
import static com.alfarays.token.enums.DurationUnit.MINUTE;
import static com.alfarays.token.enums.TokenType.ACCOUNT_ACTIVATED;
import static com.alfarays.token.enums.TokenType.FORGOT_PASSWORD;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements IAuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageService imageService;
    private final RoleRepository roleRepository;
    private final ITokenService confirmationTokenService;
    private final IMailService mailService;

    @Value("${application.email.account_activation_url}")
    private String activationUrl;

    @Value("${application.email.change_password_request_url}")
    private String changePasswordRequestUrl;

    private static final DurationUnit DURATION_UNIT = MINUTE;
    private static final int TOKEN_DURATION = 15;

    @Override
    public GlobalResponse<Void> register(RegistrationRequest request, MultipartFile image) throws MethodArgumentNotValidException {
        var user = UserMapper.toEntity(request);
        List<FieldError> fieldErrors = new ArrayList<>();

        var optionalUserByUsername = userRepository.findByUsername(request.username());
        if(optionalUserByUsername.isPresent())
            fieldErrors.add(new FieldError("user", "username", String.format("User with username '%s' already exists.", request.username())));

        var optionalUserByEmail = userRepository.findByEmail(request.email());
        if(optionalUserByEmail.isPresent())
            fieldErrors.add(new FieldError("user", "email", String.format("User with email '%s' already exists.", request.email())));

        if(!fieldErrors.isEmpty()) {
            BindingResult bindingResult = new BeanPropertyBindingResult(user, "user");
            fieldErrors.forEach(bindingResult::addError);
            MethodParameter methodParameter = new MethodParameter(
                    Arrays.stream(this.getClass().getDeclaredMethods())
                            .filter(method -> method.getName().equals("register"))
                            .findFirst()
                            .orElseThrow(), 0
            );

            throw new MethodArgumentNotValidException(methodParameter, bindingResult);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        var role = roleRepository.findByRole("USER")
                .orElseThrow(() -> new RuntimeException("Role 'user' does not exist."));
        user.setRoles(List.of(role));
        user = userRepository.save(user);

        if(null != image) {
            Image profile = imageService.upload(image, user);
            user.setProfile(profile);
        }

        String token = confirmationTokenService.create(user.getUsername(), ACCOUNT_ACTIVATED, TOKEN_DURATION, DURATION_UNIT);
        sendMail(user, token, ACCOUNT_ACTIVATION_REQUEST.content(), MailTemplate.ACCOUNT_ACTIVATION, activationUrl);

        return GlobalResponse.success(user.getUsername());
    }

    @Override
    public GlobalResponse<Void> forgetPasswordRequest(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not exists."));
        String token = confirmationTokenService.create(username, FORGOT_PASSWORD, TOKEN_DURATION, DURATION_UNIT);
        sendMail(user, token, MailSubject.FORGET_PASSWORD_REQUEST.content(), FORGOT_PASSWORD_REQUEST,
                String.format(changePasswordRequestUrl, token));
        return GlobalResponse.success("A confirmation email has been sent to your registered email address.");
    }

    @Override
    public GlobalResponse<Void> changePassword(ForgetPasswordRequest request) {
        User user = userRepository.findByUsername(request.username()).orElseThrow(() -> new RuntimeException("User not exists."));
        confirmationTokenService.validate(request.username(), request.token(), FORGOT_PASSWORD);

        if(!request.password().equals(request.confirmPassword()))
            throw new AuthorizationException("Password and confirm password do not match!");

        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);
        confirmationTokenService.invalidate(request.username(), request.token(), FORGOT_PASSWORD);
        return GlobalResponse.success("Password has been updated successfully.");
    }

    @Override
    public GlobalResponse<Void> resendConfirmationToken(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not exists."));
        String token = confirmationTokenService.create(username, ACCOUNT_ACTIVATED, TOKEN_DURATION, DURATION_UNIT);
        sendMail(user, token, ACCOUNT_ACTIVATION_REQUEST.content(), MailTemplate.ACCOUNT_ACTIVATION, activationUrl);
        return GlobalResponse.success("A new confirmation email has been sent to your registered email address.");
    }

    @Override
    public GlobalResponse<Void> activateAccount(String username, String value, TokenType type) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not exists."));
        confirmationTokenService.validate(username, value, type);
        user.setEnabled(true);
        userRepository.save(user);
        confirmationTokenService.invalidate(username, value, type);
        return GlobalResponse.success("User's account has been activated.");
    }

    @Override
    public GlobalResponse<Boolean> existsByEmail(String email) {
        return userRepository.emailExists(email)
                .map(GlobalResponse::success)
                .orElse(GlobalResponse.success(Boolean.FALSE));
    }

    @Override
    public GlobalResponse<Boolean> existsByUsername(String username) {
        return userRepository.usernameExists(username)
                .map(GlobalResponse::success)
                .orElse(GlobalResponse.success(Boolean.FALSE));
    }

    private void sendMail(User user, String token, String subject, MailTemplate template, String confirmationURL) {
        mailService.send(
                MailRequest
                        .builder()
                        .from("rupeshdulal672@gmail.com")
                        .to(user.getEmail())
                        .name(user.getFirstname() + " " + user.getLastname())
                        .username(user.getUsername())
                        .subject(subject)
                        .activationCode(token)
                        .confirmationUrl(confirmationURL)
                        .template(template)
                        .build()
        );
    }

}
