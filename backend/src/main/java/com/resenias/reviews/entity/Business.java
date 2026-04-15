package com.resenias.reviews.entity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "businesses")
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private String category;

    private String address;

    private String city;

    private String phone;

    private String email;

    private String website;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BusinessStatus status = BusinessStatus.PENDING;

    @Column(name = "rejection_reason", columnDefinition = "text")
    private String rejectionReason;

    /** Latitud (geocodificada con Nominatim/OpenStreetMap). */
    private Double lat;

    /** Longitud (geocodificada con Nominatim/OpenStreetMap). */
    private Double lng;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "business")
    private List<Product> products;

    @OneToMany(mappedBy = "business")
    private List<Review> reviews;

    public enum BusinessStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
}
