package com.teamfourpixels.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        try {
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                if (jwtTokenProvider.validateToken(token)) {
                    var authentication = jwtTokenProvider.getAuthentication(token);
                    if (authentication != null) {
                        UsernamePasswordAuthenticationToken authenticatedToken =
                                new UsernamePasswordAuthenticationToken(
                                        authentication.getPrincipal(),
                                        authentication.getCredentials(),
                                        authentication.getAuthorities()
                                );
                        authenticatedToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authenticatedToken);
                    } else {
                        log.warn("Authentication object is null for token: {}", token);
                    }
                } else {
                    log.warn("Invalid token: {}", token);
                }
            }
        } catch (Exception e) {
            log.error("Error processing JWT token: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
