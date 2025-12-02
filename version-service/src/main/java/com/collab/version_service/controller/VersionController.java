package com.collab.version_service.controller;

import com.collab.version_service.model.DocumentVersion;
import com.collab.version_service.repository.VersionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/versions")
@Tag(name = "Version Control", description = "APIs for maintaining version history, reverting documents, and tracking user contributions")
public class VersionController {

    private final VersionRepository repository;

    public VersionController(VersionRepository repository) {
        this.repository = repository;
    }

    @Operation(summary = "Save a new version", description = "Saves a new version of a document with content and editor information")
    @ApiResponse(responseCode = "201", description = "Version saved successfully",
            content = @Content(schema = @Schema(implementation = DocumentVersion.class)))
    @PostMapping
    public ResponseEntity<?> saveVersion(@RequestBody DocumentVersion version) {
        version.setTimestamp(LocalDateTime.now());
        DocumentVersion saved = repository.save(version);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Version saved successfully");
        response.put("version", saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get all versions for a document", description = "Retrieves all versions of a specific document")
    @ApiResponse(responseCode = "200", description = "List of versions retrieved successfully")
    @GetMapping("/document/{documentId}")
    public ResponseEntity<List<DocumentVersion>> getVersions(
            @Parameter(description = "Document ID", required = true) @PathVariable Long documentId) {
        List<DocumentVersion> versions = repository.findByDocumentId(documentId);
        return ResponseEntity.ok(versions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentVersion> getVersion(@PathVariable Long id) {
        return repository.findById(id)
                .map(version -> ResponseEntity.ok(version))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get version history", description = "Retrieves version history sorted by timestamp (newest first)")
    @ApiResponse(responseCode = "200", description = "Version history retrieved successfully")
    @GetMapping("/document/{documentId}/history")
    public ResponseEntity<List<DocumentVersion>> getVersionHistory(
            @Parameter(description = "Document ID", required = true) @PathVariable Long documentId) {
        List<DocumentVersion> versions = repository.findByDocumentId(documentId);
        // Sort by timestamp descending (newest first)
        versions.sort((v1, v2) -> v2.getTimestamp().compareTo(v1.getTimestamp()));
        return ResponseEntity.ok(versions);
    }

    @Operation(summary = "Revert to previous version", description = "Reverts a document to a specific previous version")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document reverted successfully"),
        @ApiResponse(responseCode = "404", description = "Version not found")
    })
    @PostMapping("/revert/{documentId}/{versionId}")
    public ResponseEntity<?> revertVersion(
            @Parameter(description = "Document ID", required = true) @PathVariable Long documentId,
            @Parameter(description = "Version ID to revert to", required = true) @PathVariable Long versionId) {
        DocumentVersion version = repository.findById(versionId).orElse(null);
        if (version != null && version.getDocumentId().equals(documentId)) {
            DocumentVersion newVersion = new DocumentVersion();
            newVersion.setDocumentId(documentId);
            newVersion.setContent(version.getContent());
            newVersion.setEditedByUserId(version.getEditedByUserId());
            newVersion.setTimestamp(LocalDateTime.now());
            newVersion.setVersionNumber(getNextVersionNumber(documentId));
            DocumentVersion saved = repository.save(newVersion);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Document reverted to version " + versionId);
            response.put("newVersion", saved);
            return ResponseEntity.ok(response);
        }
        Map<String, String> error = new HashMap<>();
        error.put("message", "Version not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @Operation(summary = "Track user contributions", description = "Tracks and displays user contributions to a document")
    @ApiResponse(responseCode = "200", description = "Contributions retrieved successfully")
    @GetMapping("/document/{documentId}/contributions")
    public ResponseEntity<Map<String, Object>> trackContributions(
            @Parameter(description = "Document ID", required = true) @PathVariable Long documentId) {
        List<DocumentVersion> versions = repository.findByDocumentId(documentId);
        
        Map<Long, Long> userContributions = versions.stream()
                .collect(Collectors.groupingBy(
                        DocumentVersion::getEditedByUserId,
                        Collectors.counting()
                ));
        
        Map<String, Object> response = new HashMap<>();
        response.put("documentId", documentId);
        response.put("totalVersions", versions.size());
        response.put("userContributions", userContributions);
        response.put("contributions", versions.stream()
                .map(v -> {
                    Map<String, Object> contrib = new HashMap<>();
                    contrib.put("userId", v.getEditedByUserId());
                    contrib.put("timestamp", v.getTimestamp());
                    contrib.put("versionId", v.getId());
                    return contrib;
                })
                .collect(Collectors.toList()));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/contributions")
    public ResponseEntity<List<DocumentVersion>> getUserContributions(@PathVariable Long userId) {
        List<DocumentVersion> allVersions = repository.findAll();
        List<DocumentVersion> userVersions = allVersions.stream()
                .filter(v -> v.getEditedByUserId().equals(userId))
                .collect(Collectors.toList());
        return ResponseEntity.ok(userVersions);
    }

    private Integer getNextVersionNumber(Long documentId) {
        List<DocumentVersion> versions = repository.findByDocumentId(documentId);
        return versions.size() + 1;
    }
}
