package com.resenias.reviews.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class UploadService {

    private final S3Client s3Client;
    private final String bucket;
    private final String publicUrl;

    // MIME type a extensión
    private static final Map<String, String> EXTENSION_MAP = Map.of(
        "image/jpeg", "jpg",
        "image/png", "png",
        "image/webp", "webp"
    );

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
        "image/jpeg",
        "image/png",
        "image/webp"
    );

    public UploadService(S3Client s3Client,
                         @Value("${r2.bucket}") String bucket,
                         @Value("${r2.public-url}") String publicUrl) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.publicUrl = publicUrl;
    }

    public String uploadPhoto(MultipartFile file, UUID userId) {
        // Validar tipo de contenido
        if (file.getContentType() == null || !ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("Tipo de archivo no permitido");
        }

        // Validar tamaño (máximo 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("Archivo mayor a 5MB");
        }

        // Obtener extensión
        String extension = EXTENSION_MAP.get(file.getContentType());

        // Generar key
        String key = "photos/" + userId + "/" + UUID.randomUUID() + "." + extension;

        try {
            // Subir a R2
            s3Client.putObject(
                PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build(),
                RequestBody.fromBytes(file.getBytes())
            );

            // Retornar URL pública
            return publicUrl + "/" + key;
        } catch (Exception e) {
            throw new RuntimeException("Error uploading file to R2", e);
        }
    }

    public List<String> uploadPhotos(List<MultipartFile> files, UUID userId) {
        if (files.size() > 3) {
            throw new BadRequestException("Máximo 3 fotos permitidas");
        }

        return files.stream()
            .map(file -> uploadPhoto(file, userId))
            .toList();
    }
}
