package com.resenias.reviews.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.resenias.reviews.dto.BusinessDto;
import com.resenias.reviews.service.BusinessService;

@RestController
@RequestMapping("/api/admin/businesses")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBusinessController {

    private final BusinessService businessService;

    public AdminBusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @GetMapping("/pending")
    public ResponseEntity<Page<BusinessDto>> listPending(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<BusinessDto> pending = businessService.findPending(pageable);
        return ResponseEntity.ok(pending);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<BusinessDto> approveBusiness(@PathVariable UUID id) {
        BusinessDto approved = businessService.approveBusiness(id);
        return ResponseEntity.ok(approved);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<BusinessDto> rejectBusiness(
        @PathVariable UUID id,
        @RequestParam(required = false) String reason) {
        BusinessDto rejected = businessService.rejectBusiness(id, reason);
        return ResponseEntity.ok(rejected);
    }
}
