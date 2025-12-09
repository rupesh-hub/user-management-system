package com.alfarays.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class BasicSecurityConfiguration {

    @Value("${application.security.token.remember_me_secret_key}")
    private String rememberMeToken;

    @Value("${application.security.token.remember_me_token_validity}")
    private int rememberMeTokenValidity;

    private static final String[] PUBLIC_URIS = {
            "authentication",
            "authentication/**",
            "/favicon.ico",
            "/roles",
            "/roles/**",
            "/permissions",
            "/permissions/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(config -> config.ignoringRequestMatchers(HttpMethod.GET.name())
                .ignoringRequestMatchers(PUBLIC_URIS));
        http.cors(
                config -> config.configurationSource(
                        request -> {
                            CorsConfiguration configuration = new CorsConfiguration();
                            return configuration;
                        }
                )
        );

        http.sessionManagement(config -> config
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                .maximumSessions(3)
                .maxSessionsPreventsLogin(true)
        );

        http.authorizeHttpRequests(
                config -> config.requestMatchers(PUBLIC_URIS)
                        .permitAll()
                        .anyRequest()
                        .authenticated()
        );

        http.formLogin(Customizer.withDefaults());
        http.httpBasic(Customizer.withDefaults());

        // Remember Me configuration
        http.rememberMe(
                config -> config.key(rememberMeToken)
                        .tokenValiditySeconds(rememberMeTokenValidity)
                        .rememberMeParameter("remember-me")
        );
        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager detailsManager() {
        return new InMemoryUserDetailsManager(
                User.withUsername("rupesh")
                        .password("{noop}rupesh")
                        .authorities("ROLE_USER")
                        .build()
        );
    }

}
