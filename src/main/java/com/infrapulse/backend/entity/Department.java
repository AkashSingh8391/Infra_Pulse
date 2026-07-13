package com.infrapulse.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "departments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = com.infrapulse.backend.enums.ComplaintCategory.class)
    @CollectionTable(name = "department_categories", joinColumns = @JoinColumn(name = "department_id"))
    @Column(name = "category")
    @Builder.Default
    private java.util.Set<com.infrapulse.backend.enums.ComplaintCategory> handledCategories = new java.util.HashSet<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
