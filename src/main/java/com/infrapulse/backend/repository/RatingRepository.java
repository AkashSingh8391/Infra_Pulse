package com.infrapulse.backend.repository;

import com.infrapulse.backend.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByComplaintId(Long complaintId);

    @Query("select avg(r.value) from Rating r where r.complaint.officer.id = :officerId")
    Double findAverageRatingForOfficer(@Param("officerId") Long officerId);
}
