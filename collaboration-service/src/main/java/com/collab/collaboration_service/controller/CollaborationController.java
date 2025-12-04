package com.collab.collaboration_service.controller;

import com.collab.collaboration_service.model.SharedDocument;
import com.collab.collaboration_service.repository.SharedDocumentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/collab")
@CrossOrigin(origins = "*")
public class CollaborationController {

    private final SharedDocumentRepository repo;
    private final RestTemplate restTemplate = new RestTemplate();

    // document-service and user-service base URLs (via gateway or direct)
    private final String DOCUMENT_SERVICE = "http://localhost:8082";
    private final String USER_SERVICE = "http://localhost:8081";

    public CollaborationController(SharedDocumentRepository repo) {
        this.repo = repo;
    }

    // Share a document with another user (by userId)
    @PostMapping("/documents/{docId}/share")
    public ResponseEntity<?> shareDocument(
            @PathVariable Long docId,
            @RequestParam Long userId,
            @RequestParam(required=false, defaultValue="edit") String permission) {

        // Optional: verify the document exists
        try {
            restTemplate.getForObject(DOCUMENT_SERVICE + "/documents/" + docId, Object.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Document not found");
        }
        // Optional: verify the user exists
        try {
            restTemplate.getForObject(USER_SERVICE + "/users/" + userId, Object.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("User not found");
        }

        // Save share record (if not exists)
        if (repo.findByDocumentIdAndSharedWithUserId(docId, userId).isEmpty()) {
            SharedDocument sd = new SharedDocument();
            sd.setDocumentId(docId);
            sd.setSharedWithUserId(userId);
            sd.setPermission(permission);
            repo.save(sd);
        }
        return ResponseEntity.ok("Shared");
    }

    // Get documents shared with given user
    @GetMapping("/shared-with/{userId}")
    public ResponseEntity<List<SharedDocument>> getSharedWithMe(@PathVariable Long userId) {
        List<SharedDocument> list = repo.findBySharedWithUserId(userId);
        return ResponseEntity.ok(list);
    }

    // Remove share
    @DeleteMapping("/documents/{docId}/share")
    public ResponseEntity<?> unshare(@PathVariable Long docId, @RequestParam Long userId) {
        repo.findByDocumentIdAndSharedWithUserId(docId, userId).ifPresent(repo::delete);
        return ResponseEntity.ok("Unshared");
    }
}
