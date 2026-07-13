package com.infrapulse.backend.repository;

import com.infrapulse.backend.entity.Department;
import com.infrapulse.backend.enums.ComplaintCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByNameIgnoreCase(String name);

    @org.springframework.data.jpa.repository.Query(
        "select d from Department d join d.handledCategories c where c = :category")
    List<Department> findByHandledCategory(@Param("category") ComplaintCategory category);
}
