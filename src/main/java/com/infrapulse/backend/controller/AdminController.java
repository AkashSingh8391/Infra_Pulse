package com.infrapulse.backend.controller;

import com.infrapulse.backend.dto.user.UpdateRoleRequest;
import com.infrapulse.backend.dto.user.UserResponse;
import com.infrapulse.backend.security.UserPrincipal;
import com.infrapulse.backend.service.AuditLogService;
import com.infrapulse.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AuditLogService auditLogService;

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getAllUsers(search, PageRequest.of(page, size)));
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> updateRole(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {
        return ResponseEntity.ok(userService.updateRole(principal.getId(), id, request.role()));
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<UserResponse> toggleStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        return ResponseEntity.ok(userService.toggleStatus(principal.getId(), id));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<Page<Map<String, Object>>> getAuditLogs(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(auditLogService.search(search, PageRequest.of(page, size)));
    }
}
