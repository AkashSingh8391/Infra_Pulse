package com.infrapulse.backend.service;

import com.infrapulse.backend.entity.AuditLog;
import com.infrapulse.backend.entity.User;
import com.infrapulse.backend.exception.ResourceNotFoundException;
import com.infrapulse.backend.repository.AuditLogRepository;
import com.infrapulse.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public void log(Long actorId, String action, String targetDescription) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        AuditLog entry = AuditLog.builder()
                .actor(actor)
                .action(action)
                .targetDescription(targetDescription)
                .build();

        auditLogRepository.save(entry);
    }

    public Page<Map<String, Object>> search(String search, Pageable pageable) {
        return auditLogRepository.search(search, pageable).map(log -> Map.of(
                "id", log.getId(),
                "actorName", log.getActor().getName(),
                "action", log.getAction(),
                "targetDescription", log.getTargetDescription() == null ? "" : log.getTargetDescription(),
                "timestamp", log.getTimestamp()
        ));
    }
}
