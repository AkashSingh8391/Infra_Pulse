package com.infrapulse.backend.repository;

import com.infrapulse.backend.entity.User;
import com.infrapulse.backend.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByResetPasswordToken(String token);

    Page<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String name, String email, Pageable pageable);

    java.util.List<User> findByRoleAndDepartmentId(Role role, Long departmentId);
    java.util.List<User> findByRole(Role role);
}
