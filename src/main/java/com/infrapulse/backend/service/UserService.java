package com.infrapulse.backend.service;

import com.infrapulse.backend.dto.user.*;
import com.infrapulse.backend.entity.User;
import com.infrapulse.backend.enums.ComplaintStatus;
import com.infrapulse.backend.enums.Role;
import com.infrapulse.backend.exception.BadRequestException;
import com.infrapulse.backend.exception.ResourceNotFoundException;
import com.infrapulse.backend.mapper.UserMapper;
import com.infrapulse.backend.repository.ComplaintRepository;
import com.infrapulse.backend.repository.RatingRepository;
import com.infrapulse.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ComplaintRepository complaintRepository;
    private final RatingRepository ratingRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getOrThrow(userId);

        if (!user.getEmail().equalsIgnoreCase(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("That email is already in use.");
        }

        user.setName(request.name());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }

        return UserMapper.toResponse(userRepository.save(user));
    }

    public UserResponse getProfile(Long userId) {
        return UserMapper.toResponse(getOrThrow(userId));
    }

    public Page<UserResponse> getAllUsers(String search, Pageable pageable) {
        String q = (search == null) ? "" : search;
        return userRepository
                .findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q, pageable)
                .map(UserMapper::toResponse);
    }

    @Transactional
    public UserResponse updateRole(Long actorId, Long targetUserId, Role role) {
        User target = getOrThrow(targetUserId);
        target.setRole(role);
        userRepository.save(target);
        auditLogService.log(actorId, "changed role", "Set " + target.getEmail() + " to " + role);
        return UserMapper.toResponse(target);
    }

    @Transactional
    public UserResponse toggleStatus(Long actorId, Long targetUserId) {
        User target = getOrThrow(targetUserId);
        target.setActive(!target.isActive());
        userRepository.save(target);
        auditLogService.log(actorId, target.isActive() ? "reactivated user" : "suspended user", target.getEmail());
        return UserMapper.toResponse(target);
    }

    public List<OfficerResponse> getOfficers(Long departmentId) {
        List<User> officers = (departmentId != null)
                ? userRepository.findByRoleAndDepartmentId(Role.OFFICER, departmentId)
                : userRepository.findByRole(Role.OFFICER);

        return officers.stream().map(this::toOfficerResponse).toList();
    }

    public List<OfficerResponse> getLeaderboard() {
        return userRepository.findByRole(Role.OFFICER).stream()
                .map(this::toOfficerResponse)
                .sorted(Comparator.comparing(
                        (OfficerResponse o) -> o.resolvedCount()).reversed())
                .limit(10)
                .toList();
    }

    private OfficerResponse toOfficerResponse(User officer) {
        long active = complaintRepository.countByOfficerIdAndStatus(officer.getId(), ComplaintStatus.IN_PROGRESS)
                + complaintRepository.countByOfficerIdAndStatus(officer.getId(), ComplaintStatus.ACCEPTED);
        long resolved = complaintRepository.countByOfficerIdAndStatus(officer.getId(), ComplaintStatus.RESOLVED);
        Double rating = ratingRepository.findAverageRatingForOfficer(officer.getId());

        return new OfficerResponse(
                officer.getId(),
                officer.getName(),
                officer.getEmail(),
                active,
                resolved,
                rating != null ? Math.round(rating * 10) / 10.0 : null,
                null // avg resolution days: left as a future enhancement once resolution timestamps are aggregated
        );
    }

    private User getOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }
}
