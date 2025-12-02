package com.collab.document_service.controller;

import com.collab.document_service.model.DocumentMessage;
import com.collab.document_service.model.Document;
import com.collab.document_service.repository.DocumentRepository;
import com.collab.document_service.service.DocumentService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.Objects;

@Controller
public class DocumentWebSocketController {

    private final DocumentRepository documentRepository;
    private final DocumentService documentService;

    public DocumentWebSocketController(DocumentRepository documentRepository, DocumentService documentService) {
        this.documentRepository = documentRepository;
        this.documentService = documentService;
    }

    @MessageMapping("/edit-document")
    @SendTo("/topic/document-updates")
    public DocumentMessage editDocument(DocumentMessage message) {
        Long docId = Objects.requireNonNull(message.getDocumentId());
        Document updated = documentRepository.findById(docId).map(doc -> {
            doc.setContent(message.getContent());
            Document saved = documentRepository.save(doc);
            // Create a new version in Version Service
            documentService.createVersion(saved, message.getEditedByUserId());
            return saved;
        }).orElseThrow(() -> new RuntimeException("Document not found: " + docId));

        // broadcast updated content to all subscribers
        return new DocumentMessage(updated.getId(), updated.getContent(), message.getEditedByUserId());
    }
}
 