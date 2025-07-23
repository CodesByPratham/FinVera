package com.pratham.finvera.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.pratham.finvera.util.JwtUtil;

import io.jsonwebtoken.JwtException;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtils;
    private final CustomUserDetailsService userDetailsService;

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

        try {
            username = jwtUtils.getUsernameFromToken(token);

            // If username is present and user not yet authenticated
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

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
        } catch (JwtException e) {
            // Save the real exception for entry point
            request.setAttribute("jwt_exception", e);
            // Let the entry point handle the response
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } catch (Exception e) {
            // Pass the exception to the entry point via request attribute
            request.setAttribute("exception", e);
            // Let Spring Security handle this and delegate to JwtAuthenticationEntryPoint
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}