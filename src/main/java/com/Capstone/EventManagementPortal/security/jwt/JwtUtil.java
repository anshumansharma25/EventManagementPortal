package com.Capstone.EventManagementPortal.security.jwt;

import com.Capstone.EventManagementPortal.model.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.file.AccessDeniedException;
import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final String SECRET_STRING = System.getenv("JWT_SECRET") != null
            ? System.getenv("JWT_SECRET")
            : "mySuperSecretKeyForJWTSigningWhichShouldBeVeryLong"; //  Fallback key for testing

    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes()); // Convert to Key

    /**
     * Generate a JWT token with email and role.
     */
    public String generateToken(String email, Role role) {
        Map<String, Object> claims = new HashMap<>();

        // Ensure role is prefixed with ROLE_ when adding to the token
        claims.put("role", "ROLE_" + role.name());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10-hour expiry
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }



    /**
     * Extract username from Authentication object.
     */
    public String extractUsername(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalArgumentException("Invalid authentication data.");
        }

        return authentication.getName();
    }

    /**
     * Extract username from token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Validate token using Authentication object.
     */
    public boolean validateToken(Authentication authentication) {
        if (authentication == null || authentication.getCredentials() == null) {
            return false;
        }
        String token = authentication.getCredentials().toString();
        return validateToken(token);
    }

    /**
     * Validate token using token value.
     */
    public boolean validateToken(String token) {
        return extractUsername(token) != null && !isTokenExpired(token);
    }

    /**
     * Validate token with user details.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public void checkOrganizerAccess(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("Access Denied: User not authenticated.");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        System.out.println("Authenticated user: " + userDetails.getUsername());
        System.out.println("Roles: " + userDetails.getAuthorities());

        boolean isOrganizer = userDetails.getAuthorities().stream()
                .anyMatch(grantedAuthority ->
                        grantedAuthority.getAuthority().equals("ROLE_ORGANIZER") ||
                                grantedAuthority.getAuthority().equals("ORGANIZER") // Fallback in case
                );

        if (!isOrganizer) {
            throw new SecurityException("Access Denied: Organizers only.");
        }
    }
    /**
     * ✅ Extract the role from JWT token.
     */
    public String extractRoleFromToken(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class)); // ✅ Extract "role" claim as String
    }

    public Role extractUserRole(Authentication authentication) {
        if (authentication == null || authentication.getCredentials() == null) {
            throw new SecurityException("Access Denied: No authentication data.");
        }
        String token = authentication.getCredentials().toString();
        String roleString = extractRoleFromToken(token);
        return Role.valueOf(roleString);
    }



    /**
     * ✅ Extract all claims from JWT token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY) // ✅ Correct key usage
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * ✅ Extract specific claim.
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * ✅ Check if the token is expired.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * ✅ Extract expiration date from token.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
