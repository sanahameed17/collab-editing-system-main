package com.collab.collaboration_service.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shared_documents", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"document_id","shared_with_user_id"})
})
public class SharedDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="document_id", nullable=false)
    private Long documentId;

    @Column(name="shared_with_user_id", nullable=false)
    private Long sharedWithUserId;

    @Column(name="permission", nullable=false)
    private String permission = "edit"; // edit/view

    @Column(name="shared_at")
    private LocalDateTime sharedAt = LocalDateTime.now();

    // getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    public Long getSharedWithUserId() { return sharedWithUserId; }
    public void setSharedWithUserId(Long sharedWithUserId) { this.sharedWithUserId = sharedWithUserId; }
    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }
    public LocalDateTime getSharedAt() { return sharedAt; }
    public void setSharedAt(LocalDateTime sharedAt) { this.sharedAt = sharedAt; }
}
