package com.resenias.reviews.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.resenias.reviews.dto.UpdateProfileDto;
import com.resenias.reviews.dto.UserDto;
import com.resenias.reviews.security.UserPrincipal;
import com.resenias.reviews.service.UserService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** Perfil público de un usuario (nombre, avatar). */
    @GetMapping("/{id}/profile")
    public ResponseEntity<UserDto> getPublicProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    /** Actualizar perfil del usuario autenticado. */
    @PutMapping("/me/profile")
    public ResponseEntity<UserDto> updateMyProfile(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody UpdateProfileDto dto) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(userService.updateProfile(principal.getId(), dto));
    }
}
