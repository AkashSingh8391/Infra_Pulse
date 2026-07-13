package com.infrapulse.backend.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Handles inbound STOMP messages published by the frontend to
 * /app/complaints/{id}/chat. Broadcasts to /topic/complaints/{id}, which both
 * the citizen's ComplaintDetail page and the officer's ComplaintUpdate page
 * are subscribed to - this is also what triggers their React Query refetch.
 */
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/complaints/{id}/chat")
    public void handleChatMessage(@DestinationVariable Long id, Map<String, Object> payload, Principal principal) {
        String senderName = "Someone";
        if (principal instanceof Authentication auth && auth.getPrincipal() instanceof com.infrapulse.backend.security.UserPrincipal up) {
            senderName = up.getUser().getName();
        }

        Map<String, Object> broadcast = Map.of(
                "type", "CHAT_MESSAGE",
                "complaintId", id,
                "authorName", senderName,
                "message", payload.getOrDefault("message", ""),
                "timestamp", LocalDateTime.now().toString()
        );

        messagingTemplate.convertAndSend("/topic/complaints/" + id, broadcast);
    }
}
