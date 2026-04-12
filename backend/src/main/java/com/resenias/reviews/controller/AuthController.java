package com.resenias.reviews.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.resenias.reviews.dto.LocalLoginDto;
import com.resenias.reviews.dto.OtpRequestDto;
import com.resenias.reviews.dto.OtpVerifyDto;
import com.resenias.reviews.dto.UserDto;
import com.resenias.reviews.entity.User;
import com.resenias.reviews.repository.UserRepository;
import com.resenias.reviews.security.JwtService;
import com.resenias.reviews.security.UserPrincipal;
import com.resenias.reviews.service.OtpJwtService;
import com.resenias.reviews.service.UserService;
import com.resenias.reviews.service.OtpExpiredException;
import com.resenias.reviews.service.OtpInvalidException;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final OtpJwtService otpService;
    private final UserService userService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(OtpJwtService otpService,
                          UserService userService,
                          JwtService jwtService,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.otpService = otpService;
        this.userService = userService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LocalLoginDto body) {
        log.info("🔐 Login attempt para: {}", body.email());
        
        User user = userRepository.findByEmail(body.email())
            .orElseThrow(() -> {
                log.warn("❌ Usuario no encontrado: {}", body.email());
                return new RuntimeException("Credenciales inválidas");
            });

        String passwordHash = user.getPasswordHash();
        if (passwordHash == null || passwordHash.isBlank() || !passwordEncoder.matches(body.password(), passwordHash)) {
            log.warn("❌ Contraseña inválida para: {}", body.email());
            throw new RuntimeException("Credenciales inválidas");
        }

        String token = jwtService.generateToken(user);
        log.info("✅ Login exitoso para: {}", body.email());
        
        return ResponseEntity.ok(Map.of(
            "token", token,
            "user", Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "name", user.getName(),
                "role", user.getRole()
            )
        ));
    }

    @PostMapping("/otp/request")
    public ResponseEntity<Map<String, Object>> requestOtp(@Valid @RequestBody OtpRequestDto body) {
        log.info("📱 OTP request para teléfono: {}", body.phone());
        
        try {
            String otpToken = otpService.generateOtpToken(body.phone());
            log.info("✅ OTP generado y enviado a: {}", body.phone());
            
            return ResponseEntity.ok(Map.of(
                "otpToken", otpToken,
                "message", "Código OTP enviado a tu teléfono",
                "expiresIn", 300  // 5 minutos en segundos
            ));
        } catch (Exception e) {
            log.error("❌ Error generando OTP para {}: {}", body.phone(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "No se pudo generar el código OTP. Intenta nuevamente."));
        }
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<Map<String, Object>> verifyOtp(@Valid @RequestBody OtpVerifyDto body,
                                                          @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            log.warn("❌ Usuario no autenticado intenta verificar OTP");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Usuario no autenticado"));
        }

        log.info("✅ Verificando OTP para usuario: {}", principal.getUsername());
        
        try {
            String phone = otpService.verifyOtpToken(body.otpToken(), body.code());
            User updatedUser = userService.markPhoneVerified(principal.getId(), phone);
            String token = jwtService.generateToken(updatedUser);
            
            log.info("✅ Teléfono verificado exitosamente para usuario: {}", principal.getUsername());
            
            return ResponseEntity.ok(Map.of(
                "token", token,
                "message", "Teléfono verificado exitosamente",
                "user", Map.of(
                    "id", updatedUser.getId(),
                    "email", updatedUser.getEmail(),
                    "phone", updatedUser.getPhone(),
                    "phoneVerified", true,
                    "status", updatedUser.getStatus()
                )
            ));
        } catch (OtpExpiredException e) {
            log.warn("⏱️ OTP expirado para usuario: {}", principal.getUsername());
            return ResponseEntity.status(HttpStatus.GONE)
                .body(Map.of("error", "Código OTP expirado. Solicita uno nuevo."));
        } catch (OtpInvalidException e) {
            log.warn("❌ OTP inválido para usuario: {}", principal.getUsername());
            if ("Token ya utilizado".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Este token ya fue utilizado. Solicita un código nuevo."));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Código OTP inválido"));
        } catch (Exception e) {
            log.error("❌ Error verificando OTP para usuario {}: {}", principal.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error verificando el código OTP"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new RuntimeException("Authenticated user required");
        }

        UserDto user = userService.getById(principal.getId());
        return ResponseEntity.ok(user);
    }
}
