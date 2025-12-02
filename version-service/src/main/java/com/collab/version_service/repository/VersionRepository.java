package com.collab.version_service.repository;

import com.collab.version_service.model.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VersionRepository extends JpaRepository<DocumentVersion, Long> {
    List<DocumentVersion> findByDocumentId(Long documentId);
}
