package com.resenias.reviews.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.resenias.reviews.security.UserPrincipal;
import com.resenias.reviews.service.UploadService;

@RestController
@RequestMapping("/api/upload")
@PreAuthorize("isAuthenticated()")
public class UploadController {

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping(value = "/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<String>> uploadPhotos(
            @RequestPart("files") List<MultipartFile> files,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<String> urls = uploadService.uploadPhotos(files, principal.getId());
        return ResponseEntity.ok(urls);
    }
}
