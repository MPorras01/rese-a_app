package com.resenias.reviews.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.AESDecrypter;
import com.nimbusds.jose.crypto.AESEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Service
public class OtpJwtService {

    private static final Logger log = LoggerFactory.getLogger(OtpJwtService.class);
    private static final Duration OTP_TTL = Duration.ofMinutes(5);

    private final SecretKey aesKey;
    private final OtpBlacklistService blacklist;
    private final Environment environment;
    private final BCryptPasswordEncoder encoder;
    private final SecureRandom random;
    private final SmsService smsService;

    public OtpJwtService(@Value("${otp.jwt.secret}") String secretHex,
                         OtpBlacklistService blacklist,
                         Environment environment,
                         SmsService smsService) {
        this.aesKey = deriveAesKey(secretHex);
        this.blacklist = blacklist;
        this.environment = environment;
        this.encoder = new BCryptPasswordEncoder(10);
        this.random = new SecureRandom();
        this.smsService = smsService;
    }

    public String generateOtpToken(String phone) {
        try {
            int code = random.nextInt(900_000) + 100_000;
            String codeStr = String.valueOf(code);
            String hash = encoder.encode(codeStr);
            Instant now = Instant.now();
            String jti = UUID.randomUUID().toString();

            // Send OTP via SMS
            boolean smsSent = smsService.sendOtpCode(phone, codeStr);
            if (!smsSent) {
                log.warn("⚠️ SMS no pudo ser enviado para {}, pero continuando con el flujo", phone);
            }

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(phone)
                .jwtID(jti)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(OTP_TTL)))
                .claim("otp_hash", hash)
                .claim("type", "OTP_VERIFY")
                .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            signedJWT.sign(new MACSigner(aesKey.getEncoded()));

            JWEHeader jweHeader = new JWEHeader.Builder(JWEAlgorithm.A256GCMKW, EncryptionMethod.A256GCM)
                .contentType("JWT")
                .build();
            JWEObject jweObject = new JWEObject(jweHeader, new Payload(signedJWT));
            jweObject.encrypt(new AESEncrypter(aesKey.getEncoded()));

            return jweObject.serialize();
        } catch (JOSEException ex) {
            throw new OtpInvalidException("Failed to generate OTP token", ex);
        }
    }

    public String verifyOtpToken(String token, String code) {
        try {
            JWEObject jweObject = JWEObject.parse(token);
            jweObject.decrypt(new AESDecrypter(aesKey.getEncoded()));

            SignedJWT signedJWT = jweObject.getPayload().toSignedJWT();
            if (signedJWT == null) {
                throw new OtpInvalidException("Invalid OTP token payload");
            }

            boolean validSignature = signedJWT.verify(new MACVerifier(aesKey.getEncoded()));
            if (!validSignature) {
                throw new OtpInvalidException("Invalid OTP token signature");
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            validateClaims(claims);

            String hash = claims.getStringClaim("otp_hash");
            if (hash == null || !encoder.matches(code, hash)) {
                throw new OtpInvalidException("OTP code is invalid");
            }

            String jti = claims.getJWTID();
            blacklist.markUsed(jti);
            return claims.getSubject();
        } catch (OtpExpiredException | OtpInvalidException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new OtpInvalidException("Failed to verify OTP token", ex);
        }
    }

    private void validateClaims(JWTClaimsSet claims) {
        Date expirationTime = claims.getExpirationTime();
        if (expirationTime == null || expirationTime.before(new Date())) {
            throw new OtpExpiredException("OTP token is expired");
        }

        Object typeClaim = claims.getClaim("type");
        String type = typeClaim == null ? null : String.valueOf(typeClaim);
        if (!"OTP_VERIFY".equals(type)) {
            throw new OtpInvalidException("Invalid OTP token type");
        }

        String jti = claims.getJWTID();
        if (jti == null || jti.isBlank()) {
            throw new OtpInvalidException("OTP token jti is missing");
        }

        if (blacklist.isUsed(jti)) {
            throw new OtpInvalidException("Token ya utilizado");
        }
    }

    private SecretKey deriveAesKey(String secretHex) {
        try {
            byte[] keyBytes = HexFormat.of().parseHex(secretHex);
            if (keyBytes.length != 32) {
                throw new OtpInvalidException("otp.jwt.secret must be a 64-hex-char AES-256 key");
            }
            return new SecretKeySpec(keyBytes, "AES");
        } catch (IllegalArgumentException ex) {
            throw new OtpInvalidException("otp.jwt.secret must be a valid hex string", ex);
        }
    }
}
