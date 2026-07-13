package com.infrapulse.backend.controller;

import com.infrapulse.backend.dto.complaint.*;
import com.infrapulse.backend.enums.ComplaintCategory;
import com.infrapulse.backend.enums.ComplaintStatus;
import com.infrapulse.backend.security.UserPrincipal;
import com.infrapulse.backend.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    // ---------------------------------------------------------------- Citizen

    @PostMapping
    public ResponseEntity<ComplaintDetailResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ComplaintCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(complaintService.create(principal.getId(), request));
    }

    @GetMapping("/mine")
    public ResponseEntity<PagedComplaintResponse> getMine(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(required = false) Integer limit) {
        int effectiveSize = (limit != null) ? limit : size;
        return ResponseEntity.ok(complaintService.getMine(principal.getId(), status, search, sort, page, effectiveSize));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComplaintDetailResponse> getById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        return ResponseEntity.ok(complaintService.getById(principal.getId(), id));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<ComplaintSummaryResponse>> getNearby(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(defaultValue = "3") double radiusKm,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(complaintService.getNearby(lat, lng, radiusKm, limit));
    }

    @GetMapping("/bookmarks")
    public ResponseEntity<List<ComplaintSummaryResponse>> getBookmarks(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(complaintService.getBookmarks(principal.getId()));
    }

    @PostMapping("/{id}/bookmark")
    public ResponseEntity<Void> bookmark(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        complaintService.addBookmark(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/bookmark")
    public ResponseEntity<Void> removeBookmark(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        complaintService.removeBookmark(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/rating")
    public ResponseEntity<Void> rate(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody RatingRequest request) {
        complaintService.rate(principal.getId(), id, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(complaintService.addComment(principal.getId(), id, request));
    }

    // ---------------------------------------------------------------- Officer

    @GetMapping("/assigned")
    public ResponseEntity<PagedComplaintResponse> getAssigned(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(required = false) Integer limit) {
        int effectiveSize = (limit != null) ? limit : size;
        return ResponseEntity.ok(complaintService.getAssigned(principal.getId(), status, page, effectiveSize));
    }

    @PatchMapping("/{id}/accept")
    public ResponseEntity<Void> accept(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        complaintService.accept(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<Void> reject(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody RejectRequest request) {
        complaintService.reject(principal.getId(), id, request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/progress")
    public ResponseEntity<Void> updateProgress(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody ProgressUpdateRequest request) {
        complaintService.updateProgress(principal.getId(), id, request);
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------------- Manager / Admin

    @GetMapping
    public ResponseEntity<PagedComplaintResponse> getAll(
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(required = false) ComplaintCategory category,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "false") boolean unassigned,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(complaintService.getAll(status, category, departmentId, unassigned, page, size));
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<Void> assign(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody AssignRequest request) {
        complaintService.assignOfficer(principal.getId(), id, request.officerId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/heatmap")
    public ResponseEntity<HeatmapResponse> getHeatmap() {
        return ResponseEntity.ok(complaintService.getHeatmap());
    }

    @GetMapping("/analytics")
    public ResponseEntity<AnalyticsResponse> getAnalytics(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "system") String scope,
            @RequestParam(defaultValue = "false") boolean detailed) {
        Long departmentId = principal.getUser().getDepartment() != null
                ? principal.getUser().getDepartment().getId() : null;
        return ResponseEntity.ok(complaintService.getAnalytics(scope, detailed, departmentId));
    }
}
