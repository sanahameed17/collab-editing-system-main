package com.collab.collaboration_service.controller;

import com.collab.collaboration_service.dto.CollaborationMessage;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class RealtimeController {
    private final SimpMessagingTemplate messagingTemplate;

    public RealtimeController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // Client sends to /app/doc/{docId}/edit
    @MessageMapping("/doc/{docId}/edit")
    public void handleEdit(@DestinationVariable Long docId, CollaborationMessage message) {
        // Broadcast to topic /topic/doc/{docId}
        messagingTemplate.convertAndSend("/topic/doc/" + docId, message);
        // Optionally: persist minimal snapshot to Document Service to keep content eventually consistent
        // e.g., call document-service PUT /documents/{docId} with content and userId
    }
}
