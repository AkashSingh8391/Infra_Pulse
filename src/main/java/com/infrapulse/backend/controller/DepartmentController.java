package com.infrapulse.backend.controller;

import com.infrapulse.backend.dto.department.DepartmentRequest;
import com.infrapulse.backend.dto.department.DepartmentResponse;
import com.infrapulse.backend.security.UserPrincipal;
import com.infrapulse.backend.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> getAll() {
        return ResponseEntity.ok(departmentService.getAll());
    }

    @PostMapping
    public ResponseEntity<DepartmentResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(departmentService.create(principal.getId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponse> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(departmentService.update(principal.getId(), id, request));
    }
}
