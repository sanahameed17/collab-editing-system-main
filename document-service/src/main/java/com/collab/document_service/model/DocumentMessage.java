package com.collab.document_service.model;

public class DocumentMessage {
    private Long documentId;
    private String content;
    private Long editedByUserId;

    public DocumentMessage() {}
    public DocumentMessage(Long documentId, String content, Long editedByUserId) {
        this.documentId = documentId;
        this.content = content;
        this.editedByUserId = editedByUserId;
    }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getEditedByUserId() { return editedByUserId; }
    public void setEditedByUserId(Long editedByUserId) { this.editedByUserId = editedByUserId; }
}
