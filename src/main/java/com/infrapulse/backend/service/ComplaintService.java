package com.infrapulse.backend.service;

import com.infrapulse.backend.dto.ai.CategorySuggestionResponse;
import com.infrapulse.backend.dto.ai.PriorityPredictionResponse;
import com.infrapulse.backend.dto.complaint.*;
import com.infrapulse.backend.entity.*;
import com.infrapulse.backend.enums.ComplaintCategory;
import com.infrapulse.backend.enums.ComplaintStatus;
import com.infrapulse.backend.enums.NotificationType;
import com.infrapulse.backend.enums.Priority;
import com.infrapulse.backend.exception.BadRequestException;
import com.infrapulse.backend.exception.ForbiddenException;
import com.infrapulse.backend.exception.ResourceNotFoundException;
import com.infrapulse.backend.mapper.ComplaintMapper;
import com.infrapulse.backend.repository.*;
import com.infrapulse.backend.websocket.RealtimeNotifier;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final RatingRepository ratingRepository;
    private final BookmarkRepository bookmarkRepository;
    private final DepartmentRepository departmentRepository;
    private final NotificationRepository notificationRepository;
    private final AiService aiService;
    private final RealtimeNotifier realtimeNotifier;

    // ---------------------------------------------------------------- Citizen

    @Transactional
    public ComplaintDetailResponse create(Long citizenId, ComplaintCreateRequest request) {
        User citizen = getUserOrThrow(citizenId);

        CategorySuggestionResponse categoryGuess = aiService.suggestCategory(request.description());
        PriorityPredictionResponse priorityGuess = aiService.predictPriority(request.description());

        Department department = departmentRepository.findByHandledCategory(request.category())
                .stream().findFirst().orElse(null);

        Complaint complaint = Complaint.builder()
                .title(request.title())
                .description(request.description())
                .category(request.category() != null ? request.category() : categoryGuess.category())
                .status(ComplaintStatus.PENDING)
                .priority(priorityGuess.priority())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .address(request.address())
                .imageUrl(request.imageUrl())
                .citizen(citizen)
                .department(department)
                .build();

        Complaint saved = complaintRepository.save(complaint);

        addHistoryEntry(saved, ComplaintStatus.PENDING, "Report submitted.", null, citizen);

        return ComplaintMapper.toDetail(saved, false, null);
    }

    @Transactional(readOnly = true)
    public PagedComplaintResponse getMine(Long citizenId, ComplaintStatus status, String search, String sort, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, resolveSort(sort));
        var result = complaintRepository.findMine(citizenId, status, blankToNull(search), pageable);

        List<ComplaintSummaryResponse> content = result.getContent().stream()
                .map(c -> ComplaintMapper.toSummary(c, isBookmarked(citizenId, c.getId())))
                .toList();

        return new PagedComplaintResponse(content, result.getTotalPages(), result.getTotalElements(), buildCitizenStats(citizenId));
    }

    @Transactional(readOnly = true)
    public List<ComplaintSummaryResponse> getNearby(Double lat, Double lng, double radiusKm, int limit) {
        List<Complaint> candidates;

        if (lat != null && lng != null) {
            double latDelta = radiusKm / 111.0; // ~111km per degree latitude
            double lngDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));
            candidates = complaintRepository.findWithinBoundingBox(
                    lat - latDelta, lat + latDelta, lng - lngDelta, lng + lngDelta);
        } else {
            // No coordinates supplied yet (geolocation not wired up on this call) -
            // fall back to the most recent complaints platform-wide.
            candidates = complaintRepository.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
        }

        return candidates.stream()
                .limit(limit)
                .map(c -> ComplaintMapper.toSummary(c, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ComplaintSummaryResponse> getBookmarks(Long userId) {
        return bookmarkRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(b -> ComplaintMapper.toSummary(b.getComplaint(), true))
                .toList();
    }

    @Transactional
    public void addBookmark(Long userId, Long complaintId) {
        if (bookmarkRepository.existsByUserIdAndComplaintId(userId, complaintId)) return;
        User user = getUserOrThrow(userId);
        Complaint complaint = getComplaintOrThrow(complaintId);
        bookmarkRepository.save(Bookmark.builder().user(user).complaint(complaint).build());
    }

    @Transactional
    public void removeBookmark(Long userId, Long complaintId) {
        bookmarkRepository.deleteByUserIdAndComplaintId(userId, complaintId);
    }

    @Transactional
    public void rate(Long userId, Long complaintId, RatingRequest request) {
        Complaint complaint = getComplaintOrThrow(complaintId);

        if (!complaint.getCitizen().getId().equals(userId)) {
            throw new ForbiddenException("Only the citizen who filed this report can rate it.");
        }
        if (complaint.getStatus() != ComplaintStatus.RESOLVED) {
            throw new BadRequestException("You can only rate a complaint once it's resolved.");
        }

        Rating rating = ratingRepository.findByComplaintId(complaintId)
                .orElse(Rating.builder().complaint(complaint).build());
        rating.setValue(request.rating());
        rating.setFeedback(request.feedback());
        ratingRepository.save(rating);
    }

    // ---------------------------------------------------------------- Comments (shared citizen/officer)

    @Transactional
    public CommentResponse addComment(Long userId, Long complaintId, CommentRequest request) {
        User author = getUserOrThrow(userId);
        Complaint complaint = getComplaintOrThrow(complaintId);

        Comment comment = commentRepository.save(Comment.builder()
                .complaint(complaint)
                .author(author)
                .message(request.message())
                .build());

        CommentResponse response = ComplaintMapper.toCommentResponse(comment);
        realtimeNotifier.complaintUpdated(complaintId, "NEW_COMMENT", response);
        notifyOtherParty(complaint, author, "New message on \"" + complaint.getTitle() + "\": " + truncate(request.message()));

        return response;
    }

    // ---------------------------------------------------------------- Officer

    @Transactional(readOnly = true)
    public PagedComplaintResponse getAssigned(Long officerId, ComplaintStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = complaintRepository.findAssigned(officerId, status, pageable);

        List<ComplaintSummaryResponse> content = result.getContent().stream()
                .map(c -> ComplaintMapper.toSummary(c, false))
                .toList();

        return new PagedComplaintResponse(content, result.getTotalPages(), result.getTotalElements(), buildOfficerStats(officerId));
    }

    @Transactional
    public void accept(Long officerId, Long complaintId) {
        Complaint complaint = getAssignedComplaintOrThrow(officerId, complaintId);
        complaint.setStatus(ComplaintStatus.ACCEPTED);
        complaintRepository.save(complaint);
        addHistoryEntry(complaint, ComplaintStatus.ACCEPTED, "Officer accepted the complaint.", null, complaint.getOfficer());
        realtimeNotifier.complaintUpdated(complaintId, "STATUS_CHANGE", complaint.getStatus());
    }

    @Transactional
    public void reject(Long officerId, Long complaintId, RejectRequest request) {
        Complaint complaint = getAssignedComplaintOrThrow(officerId, complaintId);
        complaint.setStatus(ComplaintStatus.REJECTED);
        complaint.setRejectionReason(request.reason());
        complaintRepository.save(complaint);
        addHistoryEntry(complaint, ComplaintStatus.REJECTED, request.reason(), null, complaint.getOfficer());
        realtimeNotifier.complaintUpdated(complaintId, "STATUS_CHANGE", complaint.getStatus());
    }

    @Transactional
    public void updateProgress(Long officerId, Long complaintId, ProgressUpdateRequest request) {
        Complaint complaint = getAssignedComplaintOrThrow(officerId, complaintId);
        complaint.setStatus(request.status());
        if (request.status() == ComplaintStatus.RESOLVED) {
            complaint.setResolvedAt(LocalDateTime.now());
        }
        complaintRepository.save(complaint);
        addHistoryEntry(complaint, request.status(), request.note(), request.photoUrl(), complaint.getOfficer());
        realtimeNotifier.complaintUpdated(complaintId, "STATUS_CHANGE", complaint.getStatus());

        notifyOtherParty(complaint, complaint.getOfficer(),
                "Your report \"" + complaint.getTitle() + "\" is now " + request.status().name().replace("_", " ") + ".");
    }

    // ---------------------------------------------------------------- Manager / Admin

    @Transactional(readOnly = true)
    public ComplaintDetailResponse getById(Long userId, Long complaintId) {
        Complaint complaint = getComplaintOrThrow(complaintId);
        boolean bookmarked = isBookmarked(userId, complaintId);
        Integer myRating = ratingRepository.findByComplaintId(complaintId).map(Rating::getValue).orElse(null);
        return ComplaintMapper.toDetail(complaint, bookmarked, myRating);
    }

    @Transactional(readOnly = true)
    public PagedComplaintResponse getAll(ComplaintStatus status, ComplaintCategory category, Long departmentId,
                                          boolean unassignedOnly, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        var result = unassignedOnly
                ? complaintRepository.findUnassigned(departmentId, pageable)
                : complaintRepository.findAllFiltered(status, category, departmentId, pageable);

        List<ComplaintSummaryResponse> content = result.getContent().stream()
                .map(c -> ComplaintMapper.toSummary(c, false))
                .toList();

        return new PagedComplaintResponse(content, result.getTotalPages(), result.getTotalElements(), null);
    }

    @Transactional
    public void assignOfficer(Long managerId, Long complaintId, Long officerId) {
        Complaint complaint = getComplaintOrThrow(complaintId);
        User officer = getUserOrThrow(officerId);

        complaint.setOfficer(officer);
        if (complaint.getDepartment() == null && officer.getDepartment() != null) {
            complaint.setDepartment(officer.getDepartment());
        }
        complaintRepository.save(complaint);

        addHistoryEntry(complaint, complaint.getStatus(), "Assigned to " + officer.getName() + ".", null, getUserOrThrow(managerId));
        realtimeNotifier.complaintUpdated(complaintId, "ASSIGNED", officer.getName());
        realtimeNotifier.notifyUser(officer.getId(), "You've been assigned a new complaint: " + complaint.getTitle());

        saveNotification(officer, NotificationType.ASSIGNMENT, "You've been assigned: " + complaint.getTitle(), complaintId);
    }

    public HeatmapResponse getHeatmap() {
        List<Complaint> all = complaintRepository.findAll();
        return new HeatmapResponse(all.stream().map(ComplaintMapper::toHeatmapPoint).toList());
    }

    public AnalyticsResponse getAnalytics(String scope, boolean detailed, Long departmentId) {
        AnalyticsStats stats;
        List<TrendPoint> trend = buildTrend(departmentId, 30);
        List<TrendPoint> monthlyTrend = buildMonthlyTrend(departmentId, 6);
        List<CategoryBreakdown> byCategory = buildCategoryBreakdown();

        if ("department".equals(scope) && departmentId != null) {
            long officers = userRepository.findByRoleAndDepartmentId(com.infrapulse.backend.enums.Role.OFFICER, departmentId).size();
            long active = complaintRepository.countByDepartmentIdAndStatusIn(
                    departmentId, List.of(ComplaintStatus.PENDING, ComplaintStatus.ACCEPTED, ComplaintStatus.IN_PROGRESS));
            long resolvedThisMonth = complaintRepository.countByDepartmentIdAndStatusAndResolvedAtAfter(
                    departmentId, ComplaintStatus.RESOLVED, LocalDateTime.now().minusDays(30));

            stats = new AnalyticsStats(officers, active, resolvedThisMonth, 3.2, null, null, null, null,
                    detailed ? 4.2 : null, detailed ? 6.5 : null, detailed ? 91.0 : null);
        } else {
            long totalUsers = userRepository.count();
            long departments = departmentRepository.count();
            long totalComplaints = complaintRepository.count();
            long criticalOpen = complaintRepository.countByPriorityAndStatusNot(Priority.CRITICAL, ComplaintStatus.RESOLVED);

            stats = new AnalyticsStats(null, null, null, null, totalUsers, departments, totalComplaints, criticalOpen,
                    null, null, null);
        }

        return new AnalyticsResponse(stats, trend, monthlyTrend, byCategory);
    }

    // ---------------------------------------------------------------- Internal helpers

    private ComplaintStatsResponse buildCitizenStats(Long citizenId) {
        return new ComplaintStatsResponse(
                complaintRepository.countByCitizenId(citizenId),
                complaintRepository.countByCitizenIdAndStatus(citizenId, ComplaintStatus.PENDING),
                complaintRepository.countByCitizenIdAndStatus(citizenId, ComplaintStatus.IN_PROGRESS),
                complaintRepository.countByCitizenIdAndStatus(citizenId, ComplaintStatus.RESOLVED)
        );
    }

    private ComplaintStatsResponse buildOfficerStats(Long officerId) {
        return new ComplaintStatsResponse(
                complaintRepository.countByOfficerId(officerId),
                complaintRepository.countByOfficerIdAndStatus(officerId, ComplaintStatus.PENDING),
                complaintRepository.countByOfficerIdAndStatus(officerId, ComplaintStatus.IN_PROGRESS),
                complaintRepository.countByOfficerIdAndStatusAndResolvedAtAfter(
                        officerId, ComplaintStatus.RESOLVED, LocalDateTime.now().minusDays(30))
        );
    }

    private List<TrendPoint> buildTrend(Long departmentId, int days) {
        List<Complaint> recent = complaintRepository.findByCreatedAtAfter(LocalDateTime.now().minusDays(days));
        java.util.Map<String, Long> byDay = new java.util.TreeMap<>();
        for (Complaint c : recent) {
            if (departmentId != null && (c.getDepartment() == null || !c.getDepartment().getId().equals(departmentId))) continue;
            String key = c.getCreatedAt().toLocalDate().toString();
            byDay.merge(key, 1L, Long::sum);
        }
        return byDay.entrySet().stream().map(e -> new TrendPoint(e.getKey(), e.getValue())).toList();
    }

    private List<TrendPoint> buildMonthlyTrend(Long departmentId, int months) {
        List<Complaint> recent = complaintRepository.findByCreatedAtAfter(LocalDateTime.now().minusMonths(months));
        java.util.Map<String, Long> byMonth = new java.util.TreeMap<>();
        for (Complaint c : recent) {
            if (departmentId != null && (c.getDepartment() == null || !c.getDepartment().getId().equals(departmentId))) continue;
            String key = c.getCreatedAt().getYear() + "-" + String.format("%02d", c.getCreatedAt().getMonthValue());
            byMonth.merge(key, 1L, Long::sum);
        }
        return byMonth.entrySet().stream().map(e -> new TrendPoint(e.getKey(), e.getValue())).toList();
    }

    private List<CategoryBreakdown> buildCategoryBreakdown() {
        return complaintRepository.countByCategory().stream()
                .map(row -> new CategoryBreakdown(row.getCategory().name().replace("_", " "), row.getTotal()))
                .toList();
    }

    private void addHistoryEntry(Complaint complaint, ComplaintStatus status, String note, String photoUrl, User changedBy) {
        complaint.getStatusHistory().add(ComplaintStatusHistory.builder()
                .complaint(complaint)
                .status(status)
                .note(note)
                .photoUrl(photoUrl)
                .changedBy(changedBy)
                .build());
        complaintRepository.save(complaint);
    }

    private void notifyOtherParty(Complaint complaint, User sender, String message) {
        User recipient = sender.getId().equals(complaint.getCitizen().getId()) ? complaint.getOfficer() : complaint.getCitizen();
        if (recipient == null) return;
        saveNotification(recipient, NotificationType.COMMENT, message, complaint.getId());
        realtimeNotifier.notifyUser(recipient.getId(), message);
    }

    private void saveNotification(User recipient, NotificationType type, String message, Long complaintId) {
        notificationRepository.save(Notification.builder()
                .recipient(recipient)
                .type(type)
                .message(message)
                .complaintId(complaintId)
                .build());
    }

    private boolean isBookmarked(Long userId, Long complaintId) {
        return userId != null && bookmarkRepository.existsByUserIdAndComplaintId(userId, complaintId);
    }

    private Sort resolveSort(String sort) {
        if (sort == null) return Sort.by(Sort.Direction.DESC, "createdAt");
        return switch (sort) {
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "priority" -> Sort.by(Sort.Direction.DESC, "priority");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    private String truncate(String text) {
        return text.length() > 80 ? text.substring(0, 80) + "…" : text;
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    private Complaint getComplaintOrThrow(Long id) {
        return complaintRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Complaint not found."));
    }

    private Complaint getAssignedComplaintOrThrow(Long officerId, Long complaintId) {
        Complaint complaint = getComplaintOrThrow(complaintId);
        if (complaint.getOfficer() == null || !complaint.getOfficer().getId().equals(officerId)) {
            throw new ForbiddenException("This complaint isn't assigned to you.");
        }
        return complaint;
    }
}
