package com.resenias.reviews.service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.resenias.reviews.dto.BusinessCreateDto;
import com.resenias.reviews.dto.BusinessDto;
import com.resenias.reviews.dto.BusinessUpdateDto;
import com.resenias.reviews.entity.Business;
import com.resenias.reviews.entity.User;
import com.resenias.reviews.mapper.BusinessMapper;
import com.resenias.reviews.repository.BusinessRepository;
import com.resenias.reviews.repository.ReviewRepository;
import com.resenias.reviews.repository.UserRepository;

@Service
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final BusinessMapper businessMapper;

    public BusinessService(BusinessRepository businessRepository,
                           UserRepository userRepository,
                           ReviewRepository reviewRepository,
                           BusinessMapper businessMapper) {
        this.businessRepository = businessRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.businessMapper = businessMapper;
    }

    @Transactional
    public BusinessDto createBusiness(UUID ownerId, BusinessCreateDto dto) {
        User owner = userRepository.findById(ownerId)
            .orElseThrow(() -> new RuntimeException("Owner not found"));

        boolean hasPending = !businessRepository.findByOwnerIdAndStatus(ownerId, Business.BusinessStatus.PENDING).isEmpty();
        if (hasPending) {
            throw new DuplicateBusinessException("Owner already has a pending business");
        }

        Business business = Business.builder()
            .owner(owner)
            .name(dto.name())
            .description(dto.description())
            .category(dto.category())
            .address(dto.address())
            .city(dto.city())
            .phone(dto.phone())
            .email(dto.email())
            .website(dto.website())
            .status(Business.BusinessStatus.PENDING)
            .build();

        Business saved = businessRepository.save(business);
        return toDto(saved);
    }

    @Transactional
    public BusinessDto approveBusiness(UUID businessId) {
        Business business = businessRepository.findById(businessId)
            .orElseThrow(() -> new RuntimeException("Business not found"));

        business.setStatus(Business.BusinessStatus.APPROVED);
        business.setUpdatedAt(OffsetDateTime.now());
        business.setRejectionReason(null);

        return toDto(businessRepository.save(business));
    }

    @Transactional
    public BusinessDto rejectBusiness(UUID businessId, String reason) {
        Business business = businessRepository.findById(businessId)
            .orElseThrow(() -> new RuntimeException("Business not found"));

        business.setStatus(Business.BusinessStatus.REJECTED);
        business.setRejectionReason(reason);
        business.setUpdatedAt(OffsetDateTime.now());

        return toDto(businessRepository.save(business));
    }

    @Transactional(readOnly = true)
    public Page<BusinessDto> findApproved(String search, String city, String category, Pageable pageable) {
        String searchValue = normalize(search);
        String cityValue = normalize(city);
        String categoryValue = normalize(category);

        return businessRepository.findApprovedWithFilters(searchValue, cityValue, categoryValue, pageable)
            .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public BusinessDto findById(UUID id, UUID callerId) {
        Business business = businessRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Business not found"));

        if (business.getStatus() == Business.BusinessStatus.APPROVED) {
            return toDto(business);
        }

        User caller = userRepository.findById(callerId)
            .orElseThrow(() -> new RuntimeException("Caller not found"));

        boolean isOwner = business.getOwner() != null && business.getOwner().getId().equals(callerId);
        boolean isAdmin = caller.getRole() == User.Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Not authorized to access this business");
        }

        return toDto(business);
    }

    @Transactional
    public BusinessDto updateBusiness(UUID id, UUID ownerId, BusinessUpdateDto dto) {
        Business business = businessRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Business not found"));

        boolean isOwner = business.getOwner() != null && business.getOwner().getId().equals(ownerId);
        if (!isOwner) {
            throw new AccessDeniedException("Only owner can update business");
        }

        business.setName(dto.name());
        business.setDescription(dto.description());
        business.setCategory(dto.category());
        business.setAddress(dto.address());
        business.setCity(dto.city());
        business.setPhone(dto.phone());
        business.setEmail(dto.email());
        business.setWebsite(dto.website());

        return toDto(businessRepository.save(business));
    }

    @Transactional(readOnly = true)
    public Page<BusinessDto> findPending(Pageable pageable) {
        return businessRepository.findByStatus(Business.BusinessStatus.PENDING, pageable)
            .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<BusinessDto> getMyBusiness(UUID ownerId) {
        return businessRepository.findFirstByOwnerIdOrderByCreatedAtDesc(ownerId)
            .map(this::toDto);
    }

    private BusinessDto toDto(Business business) {
        BusinessDto dto = businessMapper.toDto(business);
        Double avgRating = reviewRepository.avgRating(business.getId());

        return new BusinessDto(
            dto.id(),
            dto.ownerId(),
            dto.ownerName(),
            dto.name(),
            dto.description(),
            dto.category(),
            dto.address(),
            dto.city(),
            dto.phone(),
            dto.email(),
            dto.website(),
            dto.status(),
            dto.rejectionReason(),
            dto.createdAt(),
            dto.updatedAt(),
            avgRating
        );
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim();
    }
}
