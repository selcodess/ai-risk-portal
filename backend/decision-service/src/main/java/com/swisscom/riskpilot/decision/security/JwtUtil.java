package com.swisscom.riskpilot.decision.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<GrantedAuthority> extractAuthorities(String token) {
        Claims claims = getClaims(token);
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Check 'roles' claim (could be List<String> or comma-separated String)
        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof List<?> list) {
            for (Object item : list) {
                if (item != null) {
                    addAuthority(authorities, item.toString());
                }
            }
        } else if (rolesObj instanceof String rolesStr && !rolesStr.isBlank()) {
            for (String r : rolesStr.split(",")) {
                addAuthority(authorities, r.trim());
            }
        }

        // Check single 'role' claim
        Object singleRole = claims.get("role");
        if (singleRole instanceof String roleStr && !roleStr.isBlank()) {
            addAuthority(authorities, roleStr.trim());
        }

        // Check 'authorities' claim
        Object authObj = claims.get("authorities");
        if (authObj instanceof List<?> authList) {
            for (Object item : authList) {
                if (item instanceof Map<?, ?> map && map.containsKey("authority")) {
                    addAuthority(authorities, map.get("authority").toString());
                } else if (item != null) {
                    addAuthority(authorities, item.toString());
                }
            }
        }

        return new ArrayList<>(authorities);
    }

    private void addAuthority(Set<GrantedAuthority> authorities, String roleName) {
        if (roleName.startsWith("ROLE_")) {
            authorities.add(new SimpleGrantedAuthority(roleName));
            authorities.add(new SimpleGrantedAuthority(roleName.substring(5)));
        } else {
            authorities.add(new SimpleGrantedAuthority(roleName));
            authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
        }
    }

    public String generateToken(String username, List<String> roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 1000L * 60 * 60 * 24); // 24 hours
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }
}
