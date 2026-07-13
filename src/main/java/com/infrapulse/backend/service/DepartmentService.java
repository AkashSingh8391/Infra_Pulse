package com.infrapulse.backend.service;

import com.infrapulse.backend.dto.department.DepartmentRequest;
import com.infrapulse.backend.dto.department.DepartmentResponse;
import com.infrapulse.backend.entity.Department;
import com.infrapulse.backend.enums.ComplaintStatus;
import com.infrapulse.backend.enums.Role;
import com.infrapulse.backend.exception.BadRequestException;
import com.infrapulse.backend.exception.ResourceNotFoundException;
import com.infrapulse.backend.repository.ComplaintRepository;
import com.infrapulse.backend.repository.DepartmentRepository;
import com.infrapulse.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final ComplaintRepository complaintRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public DepartmentResponse create(Long actorId, DepartmentRequest request) {
        departmentRepository.findByNameIgnoreCase(request.name()).ifPresent(d -> {
            throw new BadRequestException("A department with this name already exists.");
        });

        Department department = Department.builder()
                .name(request.name())
                .description(request.description())
                .handledCategories(request.handledCategories() != null ? new HashSet<>(request.handledCategories()) : new HashSet<>())
                .build();

        Department saved = departmentRepository.save(department);
        auditLogService.log(actorId, "created department", saved.getName());
        return toResponse(saved);
    }

    @Transactional
    public DepartmentResponse update(Long actorId, Long id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found."));

        department.setName(request.name());
        department.setDescription(request.description());
        if (request.handledCategories() != null) {
            department.setHandledCategories(new HashSet<>(request.handledCategories()));
        }

        Department saved = departmentRepository.save(department);
        auditLogService.log(actorId, "updated department", saved.getName());
        return toResponse(saved);
    }

    public List<DepartmentResponse> getAll() {
        return departmentRepository.findAll().stream().map(this::toResponse).toList();
    }

    private DepartmentResponse toResponse(Department d) {
        long officerCount = userRepository.findByRoleAndDepartmentId(Role.OFFICER, d.getId()).size();
        long activeCount = complaintRepository.countByDepartmentIdAndStatusIn(
                d.getId(), List.of(ComplaintStatus.PENDING, ComplaintStatus.ACCEPTED, ComplaintStatus.IN_PROGRESS));

        return new DepartmentResponse(d.getId(), d.getName(), d.getDescription(), officerCount, activeCount);
    }
}
