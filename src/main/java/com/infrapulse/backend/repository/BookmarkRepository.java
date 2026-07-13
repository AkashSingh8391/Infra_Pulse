package com.infrapulse.backend.repository;

import com.infrapulse.backend.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Bookmark> findByUserIdAndComplaintId(Long userId, Long complaintId);
    boolean existsByUserIdAndComplaintId(Long userId, Long complaintId);
    void deleteByUserIdAndComplaintId(Long userId, Long complaintId);
}
