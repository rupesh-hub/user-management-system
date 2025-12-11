package com.alfarays.security;

import com.alfarays.user.model.PrincipleUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filter to populate SecurityContext from existing HttpSession.
 * Skips public URLs and relies fully on container-managed HttpSession.
 */
@Component
@Slf4j
public class CustomSessionValidationFilter extends OncePerRequestFilter {

    private static final String SPRING_SECURITY_CONTEXT_KEY = "SPRING_SECURITY_CONTEXT";

    // Public endpoints to skip filter
    private static final List<String> PUBLIC_URLS = List.of(
            "/login",
            "/authentication/**",
            "/favicon.ico"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            // 1️⃣ Skip public endpoints
            String path = request.getServletPath();
            if (isPublicUrl(path)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 2️⃣ Get existing HttpSession
            HttpSession session = request.getSession(false);
            if (session != null) {
                Object contextObj = session.getAttribute(SPRING_SECURITY_CONTEXT_KEY);

                if (contextObj instanceof SecurityContext securityContext) {
                    Authentication authentication = securityContext.getAuthentication();
                    if (authentication != null && authentication.isAuthenticated()) {
                        SecurityContextHolder.setContext(securityContext);

                        if (authentication.getPrincipal() instanceof PrincipleUser user) {
                            log.debug("HttpSession valid, SecurityContext set for user: {}", user.getUsername());
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error validating HttpSession", e);
            SecurityContextHolder.clearContext();
        }

        // 3️⃣ Continue filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Checks if the URL is public and should skip this filter.
     */
    private boolean isPublicUrl(String path) {
        return PUBLIC_URLS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}
