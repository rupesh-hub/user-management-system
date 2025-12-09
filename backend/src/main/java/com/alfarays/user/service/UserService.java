package com.alfarays.user.service;

import com.alfarays.authentication.model.RegistrationRequest;
import com.alfarays.exception.AuthorizationException;
import com.alfarays.image.entity.Image;
import com.alfarays.image.repository.ImageRepository;
import com.alfarays.image.service.ImageService;
import com.alfarays.mail.enums.MailTemplate;
import com.alfarays.mail.model.MailRequest;
import com.alfarays.mail.service.IMailService;
import com.alfarays.role.entity.Role;
import com.alfarays.role.repository.RoleRepository;
import com.alfarays.token.enums.DurationUnit;
import com.alfarays.token.service.ITokenService;
import com.alfarays.user.entity.User;
import com.alfarays.user.mapper.UserMapper;
import com.alfarays.user.model.ChangePassword;
import com.alfarays.user.model.UserFilterDTO;
import com.alfarays.user.model.UserResponse;
import com.alfarays.user.repository.UserRepository;
import com.alfarays.user.repository.UserSpecification;
import com.alfarays.util.GlobalResponse;
import com.alfarays.util.Paging;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.alfarays.mail.enums.MailSubject.PASSWORD_RESET_REQUEST;
import static com.alfarays.token.enums.DurationUnit.MINUTE;
import static com.alfarays.token.enums.TokenType.RESET_PASSWORD;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ImageRepository profileRepository;
    private final ImageService imageService;
    private final ITokenService confirmationToken;
    private final IMailService mailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${application.email.reset_password_url}")
    private String resetPasswordUrl;
    private static final DurationUnit DURATION_UNIT = MINUTE;
    private static final int TOKEN_DURATION = 15;

    @Override
    public GlobalResponse<UserResponse> byUsername(String username) {
        return userRepository.findByUsername(username)
                .map(usr -> {
                    usr.setProfile(getProfile(usr.getId()));
                    return usr;
                })
                .map(UserMapper::toResponse)
                .map(GlobalResponse::success)
                .orElseThrow(() -> new UsernameNotFoundException("User not exists."));
    }

    @Override
    public GlobalResponse<UserResponse> byEmail(String email) {
        return userRepository.findByEmail(email)
                .map(usr -> {
                    usr.setProfile(getProfile(usr.getId()));
                    return usr;
                })
                .map(UserMapper::toResponse)
                .map(GlobalResponse::success)
                .orElseThrow(() -> new UsernameNotFoundException("User not exists."));
    }

    @Override
    public GlobalResponse<UserResponse> byId(Long id) {
        return userRepository.findById(id)
                .map(usr -> {
                    usr.setProfile(getProfile(id));
                    return usr;
                })
                .map(UserMapper::toResponse)
                .map(GlobalResponse::success)
                .orElseThrow(() -> new UsernameNotFoundException("User not exists."));
    }

    @Override
    public GlobalResponse<Boolean> update(RegistrationRequest request, String username, MultipartFile profile) throws IOException {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not exists."));
        var image = getProfile(user.getId());
        if(StringUtils.isNotBlank(request.lastname()) || StringUtils.isNotEmpty(request.lastname()))
            user.setLastname(request.lastname());
        if(StringUtils.isNotBlank(request.firstname()) || StringUtils.isNotEmpty(request.firstname()))
            user.setFirstname(request.firstname());
        if(Objects.nonNull(profile)) {
            Image updatedProfile = imageService.update(profile, image);
            user.setProfile(updatedProfile);
        }
        userRepository.save(user);
        return GlobalResponse.success(Boolean.TRUE);
    }

    @Override
    public GlobalResponse<Void> changeProfilePicture(MultipartFile profile, Authentication authentication) {
        final String username = authentication.getName();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not exist."));
        try {
            imageService.update(profile, getProfile(user.getId()));
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
        return GlobalResponse.success("Profile updated successfully!");
    }

    @Override
    public GlobalResponse<Boolean> delete(String username) throws IOException {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not exists."));
        var image = getProfile(user.getId());
        imageService.delete(image);
        userRepository.delete(user);
        return GlobalResponse.success(Boolean.TRUE);
    }

    @Override
    public GlobalResponse<Boolean> assignRole(String username, String[] names) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not exists. "));
        List<Role> roles = new ArrayList<>();
        for(String name : names) {
            roles.add(roleRepository.findByRole(name)
                    .orElseThrow(() -> new AuthorizationException("Role not exists.")));
        }
        user.setRoles(roles);
        userRepository.save(user);
        return GlobalResponse.success(Boolean.TRUE);
    }

    @Override
    public GlobalResponse<Boolean> removeRole(String username, String[] names) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not exists. "));
        List<Role> roles = new ArrayList<>();
        for(String name : names) {
            roles.add(roleRepository.findByRole(name).orElse(null));
        }
        user.getRoles().removeAll(roles);
        userRepository.save(user);
        return GlobalResponse.success(Boolean.TRUE);
    }

    @Override
    public GlobalResponse<Void> resetPasswordRequest(String username) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not exists. "));
        String token = confirmationToken.create(username, RESET_PASSWORD, TOKEN_DURATION, DURATION_UNIT);
        sendMail(user, token, PASSWORD_RESET_REQUEST.content(), MailTemplate.RESET_PASSWORD);
        return GlobalResponse.success("A confirmation email has been sent to your registered email address.");
    }

    @Override
    public GlobalResponse<Void> changePassword(ChangePassword request, Authentication authentication) {
        var username = authentication.getName();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not exists. "));
        confirmationToken.validate(username, request.token(), request.type());

        if(!request.password().equals(request.confirmPassword()))
            throw new AuthorizationException("Password and new password do not match!");

        if(!passwordEncoder.matches(request.currentPassword(), user.getPassword()))
            throw new AuthorizationException("Your old password is incorrect!");

        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);
        return GlobalResponse.success("Password reset successfully!");
    }

    @Override
    public GlobalResponse<List<UserResponse>> getFilteredUsers(UserFilterDTO filter, Pageable pageable) {
        Specification<User> spec = UserSpecification.withFilter(filter);
        Sort sort = Sort.by(filter.getSortDirection(), filter.getSortBy());
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<User> userPage = userRepository.findAll(spec, pageable);

        return GlobalResponse.success(
                userPage.getContent()
                        .stream()
                        .map(UserMapper::toResponse)
                        .toList()
                ,
                Paging.builder()
                        .page(userPage.getNumber())
                        .size(userPage.getSize())
                        .totalElements(userPage.getTotalElements())
                        .totalPages(userPage.getTotalPages())
                        .isFirst(userPage.isFirst())
                        .isLast(userPage.isLast())
                        .build()
        );
    }

    private Image getProfile(Long userId) {
        return profileRepository.findByUserId(userId).orElse(null);
    }

    private void sendMail(User user, String token, String subject, MailTemplate template) {
        mailService.send(
                MailRequest
                        .builder()
                        .from("rupeshdulal672@gmail.com")
                        .to(user.getEmail())
                        .name(user.getFirstname() + " " + user.getLastname())
                        .username(user.getUsername())
                        .subject(subject)
                        .activationCode(token)
                        .confirmationUrl(resetPasswordUrl)
                        .template(template)
                        .build()
        );
    }
}
