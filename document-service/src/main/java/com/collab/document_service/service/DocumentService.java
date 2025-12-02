package com.collab.document_service.service;

import com.collab.document_service.model.Document;
import com.collab.document_service.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final RestTemplate restTemplate;

    @Value("${version.service.url:http://localhost:8083}")
    private String versionServiceUrl;

    @Autowired
    public DocumentService(DocumentRepository documentRepository, RestTemplate restTemplate) {
        this.documentRepository = documentRepository;
        // if restTemplate bean isn't present, you can create a new instance (fallback)
        this.restTemplate = Objects.requireNonNullElseGet(restTemplate, RestTemplate::new);
    }

    public Document saveDocumentWithVersion(Document document, Long userId) {
        Document saved = documentRepository.save(document);
        createVersion(saved, userId);
        return saved;
    }

    public Document updateDocumentWithVersion(Long documentId, Document updatedDocument, Long userId) {
        return documentRepository.findById(documentId).map(doc -> {
            if (updatedDocument.getTitle() != null) {
                doc.setTitle(updatedDocument.getTitle());
            }
            if (updatedDocument.getContent() != null) {
                doc.setContent(updatedDocument.getContent());
            }
            Document saved = documentRepository.save(doc);
            createVersion(saved, userId);
            return saved;
        }).orElse(null);
    }

    public void createVersion(Document document, Long userId) {
        try {
            Map<String, Object> versionData = new HashMap<>();
            versionData.put("documentId", document.getId());
            versionData.put("content", document.getContent());
            versionData.put("editedByUserId", userId);
            versionData.put("changeDescription", "Document updated");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(versionData, headers);

            restTemplate.postForEntity(
                    versionServiceUrl + "/versions",
                    request,
                    Object.class
            );
        } catch (Exception e) {
            // Log error but don't fail document save
            System.err.println("Failed to create version: " + e.getMessage());
        }
    }
}
