package com.collab.collaboration_service.repository;

import com.collab.collaboration_service.model.SharedDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SharedDocumentRepository extends JpaRepository<SharedDocument, Long> {
    List<SharedDocument> findBySharedWithUserId(Long userId);
    List<SharedDocument> findByDocumentId(Long documentId);
    Optional<SharedDocument> findByDocumentIdAndSharedWithUserId(Long documentId, Long sharedWithUserId);
}
