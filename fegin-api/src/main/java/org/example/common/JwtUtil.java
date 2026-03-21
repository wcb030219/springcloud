package org.example.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_REAL_NAME = "realName";

    private static final String DEFAULT_SECRET = "jwt-secret-change-me";
    private static final long DEFAULT_EXPIRE_MILLIS = 7L * 24 * 60 * 60 * 1000;

    private JwtUtil() {
    }

    public static String generateToken(Long userId, Integer role, String username, String realName) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, userId);
        claims.put(CLAIM_ROLE, role);
        claims.put(CLAIM_USERNAME, username);
        claims.put(CLAIM_REAL_NAME, realName);

        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiresAt = new Date(now + getExpireMillis());

        JwtBuilder builder = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(issuedAt)
                .setExpiration(expiresAt)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256);

        return builder.compact();
    }

    public static Claims parseClaims(String token) throws JwtException {
        Jws<Claims> jws = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
        return jws.getBody();
    }

    public static Long getUserId(Claims claims) {
        Object v = claims.get(CLAIM_USER_ID);
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (Exception e) {
            return null;
        }
    }

    public static String getRole(Claims claims) {
        Object v = claims.get(CLAIM_ROLE);
        return v == null ? null : String.valueOf(v);
    }

    private static long getExpireMillis() {
        String raw = System.getenv("JWT_EXPIRE_SECONDS");
        if (raw == null || raw.trim().isEmpty()) return DEFAULT_EXPIRE_MILLIS;
        try {
            long seconds = Long.parseLong(raw.trim());
            if (seconds <= 0) return DEFAULT_EXPIRE_MILLIS;
            return seconds * 1000;
        } catch (Exception e) {
            return DEFAULT_EXPIRE_MILLIS;
        }
    }

    private static SecretKey getSigningKey() {
        String secret = System.getenv("JWT_SECRET");
        if (secret == null || secret.trim().isEmpty()) {
            secret = DEFAULT_SECRET;
        }

        String trimmed = secret.trim();
        try {
            byte[] decoded = Decoders.BASE64.decode(trimmed);
            if (decoded.length >= 32) {
                return Keys.hmacShaKeyFor(decoded);
            }
        } catch (Exception ignored) {
        }

        byte[] bytes = trimmed.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            for (int i = 0; i < padded.length; i++) {
                padded[i] = bytes[i % bytes.length];
            }
            bytes = padded;
        }
        return Keys.hmacShaKeyFor(bytes);
    }
}

