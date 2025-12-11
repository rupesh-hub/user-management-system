package com.alfarays.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * MY MOTO: “I will check the credentials against my database (or any source). If correct, I will return a valid Authentication object.”
 * CustomProductionAuthenticationProvider
 * Purpose: Authenticate users at login.
 * It is called only during login (when the user submits username/password).
 * It says: “I will check the credentials against my database (or any source). If correct, I will return a valid Authentication object.”
 * Step by step flow:
 * User submits /login with username/password.
 * Spring Security uses UsernamePasswordAuthenticationFilter.
 * This filter calls: CustomProductionAuthenticationProvider.
 * Inside authenticate():
 * Loads user from DB (UserDetailsService)
 * Checks if password matches (PasswordEncoder.matches)
 * If yes → returns UsernamePasswordAuthenticationToken with user authorities
 * Spring Security stores this Authentication in:
 * SecurityContextHolder
 * And by default, also in HttpSession (so you don’t have to log in again)
 */

@Component
@Profile("production")
@RequiredArgsConstructor
public class CustomProductionAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService detailsService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        UserDetails userDetails = detailsService.loadUserByUsername(username);
        if(passwordEncoder.matches(password, userDetails.getPassword()))
            return new UsernamePasswordAuthenticationToken(username, password, userDetails.getAuthorities());
        else throw new BadCredentialsException("Invalid credentials!");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
