package com.infrapulse.backend.repository;

import com.infrapulse.backend.entity.Complaint;
import com.infrapulse.backend.enums.ComplaintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    @Query("""
        select c from Complaint c
        where c.citizen.id = :citizenId
          and (:status is null or c.status = :status)
          and (:search is null or lower(c.title) like lower(concat('%', :search, '%'))
               or lower(c.description) like lower(concat('%', :search, '%')))
        """)
    Page<Complaint> findMine(@Param("citizenId") Long citizenId,
                              @Param("status") ComplaintStatus status,
                              @Param("search") String search,
                              Pageable pageable);

    @Query("""
        select c from Complaint c
        where c.officer.id = :officerId
          and (:status is null or c.status = :status)
        """)
    Page<Complaint> findAssigned(@Param("officerId") Long officerId,
                                  @Param("status") ComplaintStatus status,
                                  Pageable pageable);

    List<Complaint> findByOfficerIdAndStatusIn(Long officerId, List<ComplaintStatus> statuses);

    @Query("""
        select c from Complaint c
        where c.latitude between :minLat and :maxLat
          and c.longitude between :minLng and :maxLng
        """)
    List<Complaint> findWithinBoundingBox(@Param("minLat") double minLat,
                                           @Param("maxLat") double maxLat,
                                           @Param("minLng") double minLng,
                                           @Param("maxLng") double maxLng);

    @Query("""
        select c from Complaint c
        where c.officer is null
          and (:departmentId is null or c.department.id = :departmentId)
        """)
    Page<Complaint> findUnassigned(@Param("departmentId") Long departmentId, Pageable pageable);

    @Query("""
        select c from Complaint c
        where (:status is null or c.status = :status)
          and (:category is null or c.category = :category)
          and (:departmentId is null or c.department.id = :departmentId)
        """)
    Page<Complaint> findAllFiltered(@Param("status") ComplaintStatus status,
                                     @Param("category") com.infrapulse.backend.enums.ComplaintCategory category,
                                     @Param("departmentId") Long departmentId,
                                     Pageable pageable);

    long countByCitizenIdAndStatus(Long citizenId, ComplaintStatus status);
    long countByCitizenId(Long citizenId);

    long countByOfficerIdAndStatus(Long officerId, ComplaintStatus status);
    long countByOfficerId(Long officerId);
    long countByOfficerIdAndStatusAndResolvedAtAfter(Long officerId, ComplaintStatus status, LocalDateTime after);

    long countByDepartmentId(Long departmentId);
    long countByDepartmentIdAndStatusIn(Long departmentId, List<ComplaintStatus> statuses);
    long countByDepartmentIdAndStatusAndResolvedAtAfter(Long departmentId, ComplaintStatus status, LocalDateTime after);

    long countByStatus(ComplaintStatus status);
    long countByPriorityAndStatusNot(com.infrapulse.backend.enums.Priority priority, ComplaintStatus status);

    List<Complaint> findByCreatedAtAfter(LocalDateTime after);

    @Query("select c.category as category, count(c) as total from Complaint c group by c.category")
    List<CategoryCount> countByCategory();

    interface CategoryCount {
        com.infrapulse.backend.enums.ComplaintCategory getCategory();
        Long getTotal();
    }
}
