package com.alfarays.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true) // enables @Secured and @PreAuthorize
@RequiredArgsConstructor
public class BasicSecurityConfiguration {

    private final CustomSessionValidationFilter sessionValidationFilter;

    // Remember-me configuration
    @Value("${application.security.token.remember_me_secret_key}")
    private String rememberMeToken;

    @Value("${application.security.token.remember_me_token_validity}")
    private int rememberMeTokenValidity;

    // CORS allowed origins
    @Value("${application.cors.endpoints}")
    private List<String> allowedOrigins;

    // Public (unauthenticated) endpoints
    private static final String[] PUBLIC_URIS = {
            "/login",
            "/login/**",
            "/favicon.ico",
            "/authentication",
            "/authentication/**",
            "/roles",
            "/roles/**",
            "/permissions",
            "/permissions/**",
            "/auth/**",
            "/favicon.ico"
    };

    /**
     * ==========================================================
     * MAIN SPRING SECURITY FILTER CHAIN CONFIGURATION
     * ==========================================================
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        /* ----------------------------------------------------------
         * 1. CSRF CONFIGURATION
         * ---------------------------------------------------------- */
        CsrfTokenRequestAttributeHandler csrfAttrHandler = new CsrfTokenRequestAttributeHandler();

        http.csrf(csrf -> csrf
                .csrfTokenRequestHandler(csrfAttrHandler)
                .ignoringRequestMatchers(HttpMethod.GET.name()) // ignore GET requests entirely
                .ignoringRequestMatchers(PUBLIC_URIS)
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()) // expose token to SPA
        );

        /* ----------------------------------------------------------
         * 2. CORS CONFIGURATION
         * ---------------------------------------------------------- */
        http.cors(config -> config.configurationSource((HttpServletRequest req) -> {
            CorsConfiguration cors = new CorsConfiguration();
            cors.setAllowCredentials(true);
            cors.setAllowedOrigins(allowedOrigins);
            cors.setAllowedHeaders(
                    List.of(
                            "Origin",
                            "Content-Type",
                            "Accept",
                            "Authorization",
                            "X-Requested-With",
                            "X-XSRF-TOKEN"
                    )
            );
            cors.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
            cors.setMaxAge(3600L);
            cors.setExposedHeaders(List.of("Authorization"));
            return cors;
        }));

        /* ----------------------------------------------------------
         * 3. CHANNEL SECURITY (HTTPS or HTTP)
         * ---------------------------------------------------------- */
        http.requiresChannel(config -> config
                .anyRequest().requiresInsecure() // change to requiresSecure() if using HTTPS
        );

        /* ----------------------------------------------------------
         * 4. SESSION MANAGEMENT
         * ---------------------------------------------------------- */
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                .invalidSessionUrl("/invalid-session")
                .maximumSessions(3)
                .maxSessionsPreventsLogin(true)
        );

        /* ----------------------------------------------------------
         * 5. REQUEST AUTHORIZATION RULES
         * ---------------------------------------------------------- */
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_URIS).permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // allow preflight
                .anyRequest().authenticated()
        );

        /* ----------------------------------------------------------
         * 6. FORM LOGIN
         * IF YOU WANT CLIENT SIDE LOGIN PAGE RENDERING THEN WE MUST
         * DISABLE FORM LOGIN
         * ---------------------------------------------------------- */
        http.formLogin(login -> login
                .loginPage("/login")                   // custom login page
                .defaultSuccessUrl("/dashboard", true)      // after login success
                .failureUrl("/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
        );

        /* ----------------------------------------------------------
         * 7. LOGOUT
         * ---------------------------------------------------------- */
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .deleteCookies("JSESSIONID", "remember-me")
                .clearAuthentication(true)
                .invalidateHttpSession(true)
                .logoutSuccessUrl("/login?logout=true")
        );

        /* ----------------------------------------------------------
         * 8. HTTP BASIC (API compatibility)
         * ---------------------------------------------------------- */
        http.httpBasic(config ->
                config.authenticationEntryPoint(new CustomBasicAuthenticationEntryPoint())
        );

        /* ----------------------------------------------------------
         * 9. EXCEPTION HANDLING
         * ---------------------------------------------------------- */
        http.exceptionHandling(ex -> ex
                .accessDeniedHandler(new CustomAccessDeniedHandler())
        );

        /* ----------------------------------------------------------
         * 10. CUSTOM FILTERS (example)
         * ---------------------------------------------------------- */
        http.addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class);
        //http.addFilterBefore(sessionValidationFilter, BasicAuthenticationFilter.class);

        /* ----------------------------------------------------------
         * 11. REMEMBER-ME CONFIGURATION
         * ---------------------------------------------------------- */
        http.rememberMe(remember -> remember
                .key(rememberMeToken)
                .tokenValiditySeconds(rememberMeTokenValidity)
                .rememberMeParameter("remember-me")
        );

        /* ----------------------------------------------------------
         * 12. SECURITY HEADERS
         * ---------------------------------------------------------- */
        http.headers(headers -> headers
                .xssProtection(Customizer.withDefaults())
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                .httpStrictTransportSecurity(hsts -> hsts
                        .includeSubDomains(true)
                        .maxAgeInSeconds(31536000))
        );

        /* ----------------------------------------------------------
         * OPTIONAL (Enable/Comment out when needed)
         * ---------------------------------------------------------- */

        // OAuth2 Login
        // http.oauth2Login(Customizer.withDefaults());

        // JWT Resource Server
        // http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        /* ----------------------------------------------------------
         * BUILD FILTER CHAIN
         * ---------------------------------------------------------- */
        return http.build();
    }

    /**
     * ==========================================================
     * PASSWORD ENCODER (Delegating)
     * ==========================================================
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * ==========================================================
     * COMPROMISED PASSWORD CHECKER
     * ==========================================================
     */
    @Bean
    public CompromisedPasswordChecker compromisedPasswordChecker() {
        return new HaveIBeenPwnedRestApiPasswordChecker();
    }

    @Bean
    @Primary
    public InMemoryUserDetailsManager inMemoryUserDetailsManager() {
        return new InMemoryUserDetailsManager(
                User.withUsername("Rupesh@2053")
                        .password("{noop}Rupesh@2053")   // no encoding
                        .roles("USER")                  // Spring converts ROLE_USER internally
                        .build()
        );
    }

    /**
     * HttpBasic Authentication
     * @param configuration
     * @return
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

}
