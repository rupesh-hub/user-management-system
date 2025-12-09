package com.alfarays.authentication.resource;

import com.alfarays.authentication.model.ForgetPasswordRequest;
import com.alfarays.authentication.model.RegistrationRequest;
import com.alfarays.authentication.service.IAuthenticationService;
import com.alfarays.token.enums.TokenType;
import com.alfarays.util.GlobalResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@RestController
@RequestMapping("/authentication")
@RequiredArgsConstructor
public class AuthenticationResource {

    private final IAuthenticationService authenticationService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GlobalResponse<Void>> register(@Valid RegistrationRequest request,
                                                         @RequestParam(required = false, name = "profile") MultipartFile profile)
            throws MethodArgumentNotValidException {
        return ResponseEntity.ok(authenticationService.register(request, profile));
    }

    //TODO:
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GlobalResponse<Void>> logout() {
        return ResponseEntity.ok(null);
    }

    @GetMapping("/forgot-password")
    public ResponseEntity<GlobalResponse<Void>> forgetPassword(
            @RequestParam("username") String username
    ) {
        GlobalResponse<Void> response = authenticationService.forgetPasswordRequest(username);
        if(Objects.equals(response.getCode(), "500"))
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<GlobalResponse<Void>> resetPassword(@RequestBody ForgetPasswordRequest request) {
        GlobalResponse<Void> response = authenticationService.changePassword(request);
        if(Objects.equals(response.getCode(), "500"))
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate-confirmation-token")
    public ResponseEntity<GlobalResponse<Void>> validateConfirmationToken(
            @RequestParam String token,
            @RequestParam("type") TokenType type,
            @RequestParam("username") String username
    ) {
        GlobalResponse<Void> globalResponse = authenticationService.activateAccount(username, token, type);
        if(Objects.equals(globalResponse.getCode(), "500"))
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(globalResponse);
        return ResponseEntity.ok(globalResponse);
    }


    @GetMapping("/resend-confirmation-token")
    public ResponseEntity<GlobalResponse<Void>> resendConfirmationToken(
            @RequestParam("username") String username
    ) {
        GlobalResponse<Void> response = authenticationService.resendConfirmationToken(username);
        if(Objects.equals(response.getCode(), "500"))
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/exists-by-username")
    public ResponseEntity<GlobalResponse<Boolean>> existsByUsername(@RequestParam("username") String username) {
        return ResponseEntity.ok(authenticationService.existsByUsername(username));
    }

    @GetMapping("/exists-by-email")
    public ResponseEntity<GlobalResponse<Boolean>> existsByEmail(@RequestParam("email") String email) {
        return ResponseEntity.ok(authenticationService.existsByEmail(email));
    }

}
