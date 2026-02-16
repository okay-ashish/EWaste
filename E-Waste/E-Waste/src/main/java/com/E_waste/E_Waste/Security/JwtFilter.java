package com.E_waste.E_Waste.Security;


import com.E_waste.E_Waste.Service.CustomUserDetailsService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import com.E_waste.E_Waste.Service.JwtService;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    // Public endpoints - keep in sync with SecurityConfig
    private static final String[] PUBLIC_PATHS = new String[] {
            "/api/auth/login",
            "/api/users/login",
            "/api/users/initiate",
            "/api/auth/send-otp",
            "/api/auth/verify-otp",
            "/api/users/verify"
    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        for (String p : PUBLIC_PATHS) {
            if (path.startsWith(p)) return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String email = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                email = jwtService.extractEmail(token);
            } catch (JwtException e) {
                email = null;
            }
        }

        if (email != null && token != null && jwtService.validateToken(token)) {
            try {
                // loadUserByUsername returns a UserDetails; in your app it should return your User entity
                var userDetails = userDetailsService.loadUserByUsername(email);
                if (jwtService.validateToken(token, userDetails)) {
                    // userDetails is your User (implements UserDetails) so principal will be user object
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    SecurityContextHolder.clearContext();
                }
            } catch (Exception ex) {
                // cannot load user or other error
                SecurityContextHolder.clearContext();
            }
        } else {
            // no token or invalid token for protected endpoint
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
