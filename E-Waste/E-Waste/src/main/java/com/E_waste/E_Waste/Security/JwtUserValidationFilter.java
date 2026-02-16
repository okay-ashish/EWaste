package com.E_waste.E_Waste.Security;




import com.E_waste.E_Waste.Service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.E_waste.E_Waste.Service.UserService;

import java.io.IOException;

@Component
public class JwtUserValidationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtUserValidationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Skip if no token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        Long tokenUserId;
        try {
            tokenUserId = jwtService.extractId(token);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid Token");
            return;
        }

        // === Check URL parameters (ex: ?userId=3)
        String userIdParam = request.getParameter("userId");
        if (userIdParam != null) {
            Long urlUserId = Long.valueOf(userIdParam);

            if (!tokenUserId.equals(urlUserId)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Forbidden: Cannot access other user's data");
                return;
            }
        }

        // === Check path variables manually if needed (future extensions)
        // Example: /api/users/3
        // You can expand this part later if your routes need it.

        filterChain.doFilter(request, response);
    }
}


