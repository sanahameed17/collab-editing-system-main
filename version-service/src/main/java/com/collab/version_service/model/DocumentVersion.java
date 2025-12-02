package com.collab.version_service.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "document_versions")
public class DocumentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "edited_by_user_id")
    private Long editedByUserId;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "version_number")
    private Integer versionNumber;

    @Column(name = "change_description", length = 500)
    private String changeDescription;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public Long getEditedByUserId() { return editedByUserId; }
    public void setEditedByUserId(Long editedByUserId) { this.editedByUserId = editedByUserId; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }
    
    public String getChangeDescription() { return changeDescription; }
    public void setChangeDescription(String changeDescription) { this.changeDescription = changeDescription; }
}
