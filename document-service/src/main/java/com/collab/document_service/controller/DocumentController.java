package com.collab.document_service.controller;

import com.collab.document_service.model.Document;
import com.collab.document_service.repository.DocumentRepository;
import com.collab.document_service.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/documents")
@Tag(name = "Document Editing", description = "APIs for creating, editing, and managing documents")
public class DocumentController {

    private final DocumentRepository documentRepository;
    private final DocumentService documentService;

    public DocumentController(DocumentRepository documentRepository, DocumentService documentService) {
        this.documentRepository = documentRepository;
        this.documentService = documentService;
    }

    @Operation(summary = "Create a new document")
    @ApiResponse(responseCode = "201", description = "Document created successfully")
    @PostMapping
    public ResponseEntity<Document> createDocument(@RequestBody Document document) {
        Document saved = documentRepository.save(document);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(summary = "Get all documents")
    @GetMapping
    public ResponseEntity<List<Document>> getAllDocuments() {
        return ResponseEntity.ok(documentRepository.findAll());
    }

    @Operation(summary = "Get document by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocument(
            @Parameter(description = "Document ID", required = true) @PathVariable Long id) {
        Optional<Document> doc = documentRepository.findById(Objects.requireNonNull(id));
        return doc.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get documents by owner ID")
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<Document>> getDocumentsByOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(documentRepository.findByOwnerId(Objects.requireNonNull(ownerId)));
    }

    @Operation(summary = "Update a document (for backend API)")
    @PutMapping("/{id}")
    public ResponseEntity<Document> updateDocument(
            @PathVariable Long id,
            @RequestBody Document updatedDocument,
            @RequestParam(required = false) Long userId) {

        return documentRepository.findById(id).map(doc -> {
            if (updatedDocument.getTitle() != null) doc.setTitle(updatedDocument.getTitle());
            if (updatedDocument.getContent() != null) doc.setContent(updatedDocument.getContent());
            if (updatedDocument.getOwnerId() != null) doc.setOwnerId(updatedDocument.getOwnerId());

            Document saved = documentRepository.save(doc);

            // create version if userId is provided
            if (userId != null) {
                documentService.createVersion(saved, userId);
            }

            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete a document")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        if (documentRepository.existsById(id)) {
            documentRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
