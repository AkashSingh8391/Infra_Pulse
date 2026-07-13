package com.infrapulse.backend.config;

import com.infrapulse.backend.entity.Department;
import com.infrapulse.backend.entity.User;
import com.infrapulse.backend.enums.ComplaintCategory;
import com.infrapulse.backend.enums.Role;
import com.infrapulse.backend.repository.DepartmentRepository;
import com.infrapulse.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-email:admin@infrapulse.app}")
    private String adminEmail;

    @Value("${app.seed.admin-password:Admin@123}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        seedDepartments();
        seedAdmin();
    }

    private void seedDepartments() {
        if (departmentRepository.count() > 0) return;

        departmentRepository.save(Department.builder()
                .name("Roads & Traffic")
                .description("Potholes, damaged roads, traffic signals, illegal parking")
                .handledCategories(Set.of(
                        ComplaintCategory.POTHOLE, ComplaintCategory.DAMAGED_ROAD,
                        ComplaintCategory.TRAFFIC_SIGNAL, ComplaintCategory.ILLEGAL_PARKING))
                .build());

        departmentRepository.save(Department.builder()
                .name("Sanitation")
                .description("Garbage dumping, sewer overflow")
                .handledCategories(Set.of(ComplaintCategory.GARBAGE, ComplaintCategory.SEWER_OVERFLOW))
                .build());

        departmentRepository.save(Department.builder()
                .name("Utilities")
                .description("Street lights, water leakage")
                .handledCategories(Set.of(ComplaintCategory.STREET_LIGHT, ComplaintCategory.WATER_LEAKAGE))
                .build());

        departmentRepository.save(Department.builder()
                .name("Parks & Public Property")
                .description("Fallen trees, public property damage, and anything uncategorized")
                .handledCategories(Set.of(
                        ComplaintCategory.FALLEN_TREE, ComplaintCategory.PUBLIC_PROPERTY_DAMAGE,
                        ComplaintCategory.OTHER))
                .build());

        log.info("Seeded 4 starter departments.");
    }

    private void seedAdmin() {
        if (userRepository.existsByEmail(adminEmail)) return;

        userRepository.save(User.builder()
                .name("InfraPulse Admin")
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .active(true)
                .build());

        log.info("Seeded default admin account -> email: {} / password: {} (change this immediately in production)",
                adminEmail, adminPassword);
    }
}
