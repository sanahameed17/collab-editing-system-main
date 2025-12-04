package com.collab.collaboration_service.dto;

public class CollaborationMessage {
    private Long userId;
    private String username;
    private String content; // the whole content or delta depending on approach
    private String op; // optional: "patch" / "full"
    // getters/setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getOp() { return op; }
    public void setOp(String op) { this.op = op; }
}
