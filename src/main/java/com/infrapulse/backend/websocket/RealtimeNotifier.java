package com.infrapulse.backend.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RealtimeNotifier {

    private final SimpMessagingTemplate messagingTemplate;

    public void complaintUpdated(Long complaintId, String eventType, Object payload) {
        messagingTemplate.convertAndSend(
                "/topic/complaints/" + complaintId,
                Map.of("type", eventType, "complaintId", complaintId, "payload", payload)
        );
    }

    public void notifyUser(Long userId, Object payload) {
        messagingTemplate.convertAndSendToUser(String.valueOf(userId), "/queue/notifications", payload);
    }
}
