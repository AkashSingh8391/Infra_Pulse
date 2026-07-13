package com.infrapulse.backend.repository;

import com.infrapulse.backend.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("""
        select a from AuditLog a
        where :search is null
           or lower(a.actor.name) like lower(concat('%', :search, '%'))
           or lower(a.action) like lower(concat('%', :search, '%'))
        order by a.timestamp desc
        """)
    Page<AuditLog> search(@Param("search") String search, Pageable pageable);
}
