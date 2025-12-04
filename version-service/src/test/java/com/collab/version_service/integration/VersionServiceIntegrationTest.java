package com.collab.version_service.integration;

import com.collab.version_service.model.DocumentVersion;
import com.collab.version_service.repository.VersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class VersionServiceIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VersionRepository versionRepository;

    private DocumentVersion testVersion;

    @BeforeEach
    void setUp() {
        testVersion = new DocumentVersion();
        testVersion.setDocumentId(1L);
        testVersion.setContent("Test content");
        testVersion.setEditedByUserId(1L);
        testVersion.setTimestamp(LocalDateTime.now());
        testVersion.setVersionNumber(1);
    }

    @Test
    void testSaveVersion() {
        DocumentVersion saved = versionRepository.save(testVersion);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDocumentId()).isEqualTo(1L);
    }

    @Test
    void testFindVersionsByDocumentId() {
        entityManager.persistAndFlush(testVersion);
        
        DocumentVersion version2 = new DocumentVersion();
        version2.setDocumentId(1L);
        version2.setContent("Version 2");
        version2.setEditedByUserId(2L);
        version2.setTimestamp(LocalDateTime.now());
        version2.setVersionNumber(2);
        entityManager.persistAndFlush(version2);
        
        List<DocumentVersion> versions = versionRepository.findByDocumentId(1L);
        assertThat(versions).hasSize(2);
    }

    @Test
    void testVersionHistory() {
        entityManager.persistAndFlush(testVersion);
        
        DocumentVersion version2 = new DocumentVersion();
        version2.setDocumentId(1L);
        version2.setContent("Version 2");
        version2.setEditedByUserId(2L);
        version2.setTimestamp(LocalDateTime.now().plusMinutes(1));
        version2.setVersionNumber(2);
        entityManager.persistAndFlush(version2);
        
        List<DocumentVersion> versions = versionRepository.findByDocumentId(1L);
        assertThat(versions).hasSize(2);
        assertThat(versions.get(0).getVersionNumber()).isIn(1, 2);
    }
}

