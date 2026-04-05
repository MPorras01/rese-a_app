package com.resenias.reviews.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.resenias.reviews.dto.OtpRequestDto;
import com.resenias.reviews.dto.OtpVerifyDto;
import com.resenias.reviews.dto.UserDto;
import com.resenias.reviews.entity.User;
import com.resenias.reviews.security.JwtService;
import com.resenias.reviews.security.UserPrincipal;
import com.resenias.reviews.service.OtpJwtService;
import com.resenias.reviews.service.UserService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final OtpJwtService otpService;
    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(OtpJwtService otpService, UserService userService, JwtService jwtService) {
        this.otpService = otpService;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/otp/request")
    public ResponseEntity<Map<String, String>> requestOtp(@Valid @RequestBody OtpRequestDto body) {
        String otpToken = otpService.generateOtpToken(body.phone());
        return ResponseEntity.ok(Map.of("otpToken", otpToken));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<Map<String, String>> verifyOtp(@Valid @RequestBody OtpVerifyDto body,
                                                          @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new RuntimeException("Authenticated user required");
        }

        String phone = otpService.verifyOtpToken(body.otpToken(), body.code());
        User updatedUser = userService.markPhoneVerified(principal.getId(), phone);

        String token = jwtService.generateToken(updatedUser);
        return ResponseEntity.ok(Map.of("token", token));
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
