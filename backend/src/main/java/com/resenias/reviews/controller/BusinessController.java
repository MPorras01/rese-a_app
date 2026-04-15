package com.resenias.reviews.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.resenias.reviews.dto.BusinessCreateDto;
import com.resenias.reviews.dto.BusinessDto;
import com.resenias.reviews.dto.BusinessUpdateDto;
import com.resenias.reviews.security.UserPrincipal;
import com.resenias.reviews.service.BusinessService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/businesses")
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @GetMapping
    public ResponseEntity<Page<BusinessDto>> listApproved(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String city,
        @RequestParam(required = false) String category,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "9") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(businessService.findApproved(search, city, category, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusinessDto> getById(@PathVariable UUID id,
                                               @AuthenticationPrincipal UserPrincipal principal) {
        UUID callerId = principal == null ? null : principal.getId();
        return ResponseEntity.ok(businessService.findById(id, callerId));
    }

    @GetMapping("/me")
    public ResponseEntity<BusinessDto> getMyBusiness(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new RuntimeException("Authenticated user required");
        }

        return businessService.getMyBusiness(principal.getId())
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.ok(null));
    }

    @PostMapping
    public ResponseEntity<BusinessDto> createBusiness(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody BusinessCreateDto dto) {
        if (principal == null) {
            throw new RuntimeException("Authenticated user required");
        }

        BusinessDto created = businessService.createBusiness(principal.getId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BusinessDto> updateBusiness(
        @PathVariable UUID id,
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody BusinessUpdateDto dto) {
        if (principal == null) {
            throw new RuntimeException("Authenticated user required");
        }

        BusinessDto updated = businessService.updateBusiness(id, principal.getId(), dto);
        return ResponseEntity.ok(updated);
    }
}
