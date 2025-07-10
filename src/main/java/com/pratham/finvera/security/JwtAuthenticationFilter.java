package com.pratham.finvera.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.pratham.finvera.util.JwtUtils;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;
    private final AdminDetailsService adminDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Retrieves the Authorization header from the incoming HTTP request
        final String authHeader = request.getHeader("Authorization");
        final String token;
        final String username;

        /*
         * Check for token in Authorization header
         * Early exit: If the header is missing or doesn't start with "Bearer ", skip
         * JWT logic and let the request pass down the chain.
         * This allows public endpoints to work (like /login, /register).
         */
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        token = authHeader.substring(7); // Remove "Bearer " prefix
        username = jwtUtils.getUsernameFromToken(token);

        // If username is present and user not yet authenticated
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails;
            String role = jwtUtils.getRoleFromToken(token);
            if ("ROLE_ADMIN".equals(role)) {
                userDetails = adminDetailsService.loadUserByUsername(username);
            } else {
                userDetails = userDetailsService.loadUserByUsername(username);
            }
            
            // Checks if the token is valid or not
            if (jwtUtils.isTokenValid(token)) {
                // Creates a Spring Security authentication object
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
                        null, userDetails.getAuthorities());

                // Adds request-specific information to the authentication object (like IP
                // address, session ID, etc.).
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Sets the current request as authenticated
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
