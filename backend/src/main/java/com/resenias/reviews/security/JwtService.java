package com.resenias.reviews.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.resenias.reviews.entity.User;

@Service
public class JwtService {

    private static final Duration TOKEN_TTL = Duration.ofDays(7);

    private final SecretKey secretKey;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.secretKey = deriveHmacSha256Key(secret);
    }

    public String generateToken(User user) {
        try {
            Instant now = Instant.now();
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .claim("role", user.getRole().name())
                .claim("status", user.getStatus().name())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(TOKEN_TTL)))
                .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(new MACSigner(secretKey.getEncoded()));

            return signedJWT.serialize();
        } catch (JOSEException ex) {
            throw new JwtAuthException("Could not generate JWT token", ex);
        }
    }

    public JWTClaimsSet validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            boolean validSignature = signedJWT.verify(new MACVerifier(secretKey.getEncoded()));
            if (!validSignature) {
                throw new JwtAuthException("Invalid JWT signature");
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            Date expirationTime = claims.getExpirationTime();
            if (expirationTime == null || expirationTime.before(new Date())) {
                throw new JwtAuthException("JWT token is expired");
            }

            return claims;
        } catch (JwtAuthException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JwtAuthException("Invalid JWT token", ex);
        }
    }

    public UUID getUserIdFromToken(String token) {
        JWTClaimsSet claims = validateToken(token);
        String subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new JwtAuthException("JWT subject is missing");
        }

        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException ex) {
            throw new JwtAuthException("JWT subject is not a valid UUID", ex);
        }
    }

    private SecretKey deriveHmacSha256Key(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new JwtAuthException("JWT secret must not be empty");
        }

        return new SecretKeySpec(decodeSecret(secret), "HmacSHA256");
    }

    private byte[] decodeSecret(String secret) {
        try {
            return Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException ex) {
            return secret.getBytes(StandardCharsets.UTF_8);
        }
    }
}
