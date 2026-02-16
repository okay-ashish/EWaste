package com.E_waste.E_Waste.Service;



import com.E_waste.E_Waste.Entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    private static final long EXPIRATION_TIME = 1000L * 60 * 60 * 24; // 24 hours

    // WARNING: Move this to application.properties in production
    private final String secret =
            "ewaste-secret-key-ewaste-secret-key-extended-for-hs256-algorithm-security";

    private final Key secretKey = Keys.hmacShaKeyFor(secret.getBytes());

    // --------------------------------------------------------------
    // 🔥 Generate token with mandatory claims for secure validation
    // --------------------------------------------------------------
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();

        claims.put("id", user.getId());
        claims.put("email", user.getEmail());
//        claims.put("role", user.getRole());
//        claims.put("username", user.getUsernameField());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())          // subject = email
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // --------------------------------------------------------------
    // 🔥 Validate token structure & signature
    // --------------------------------------------------------------
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // --------------------------------------------------------------
    // 🔥 Get all claims safely
    // --------------------------------------------------------------
    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // --------------------------------------------------------------
    // 🔥 Extract required fields
    // --------------------------------------------------------------

    public String extractEmail(String token) {
        try {
//            System.out.println("Extracted Email = " + parseClaims(token).getSubject());

            return parseClaims(token).getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public Long extractId(String token) {
        try {
            Object id = parseClaims(token).get("id");
            if (id instanceof Number) return ((Number) id).longValue();
            if (id instanceof String) return Long.parseLong((String) id);
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public String extractRole(String token) {
        try {
            Object role = parseClaims(token).get("role");
            return role != null ? role.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public String extractUsername(String token) {
        try {
            Object u = parseClaims(token).get("username");
            return u != null ? u.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    // --------------------------------------------------------------
    // 🔥 Token expiration check
    // --------------------------------------------------------------
    private boolean isTokenExpired(String token) {
        try {
            Date exp = parseClaims(token).getExpiration();
            return exp == null || exp.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    // --------------------------------------------------------------
    // 🔥 Validate token against Spring UserDetails (optional)
    // --------------------------------------------------------------
    public boolean validateToken(String token, org.springframework.security.core.userdetails.UserDetails details) {
        String email = extractEmail(token);
        return email != null && email.equals(details.getUsername()) && !isTokenExpired(token);
    }
}
