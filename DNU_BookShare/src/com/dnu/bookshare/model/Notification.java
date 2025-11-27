// ========== Notification.java ==========
package src.com.dnu.bookshare.model;

import java.time.LocalDateTime;

public class Notification {
    private String notificationId;
    private String userId;
    private NotificationType type;
    private String title;
    private String message;
    private String relatedId;
    private boolean isRead;
    private LocalDateTime createdAt;
    
    public enum NotificationType {
        NEW_REQUEST, REQUEST_APPROVED, REQUEST_REJECTED,
        BOOK_DELIVERED, BOOK_RETURNED, RETURN_REMINDER,
        NEW_MESSAGE, NEW_REVIEW, SYSTEM_ANNOUNCEMENT
    }
    
    public Notification(String userId, NotificationType type, String title, String message) {
        this.notificationId = generateId();
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }
    
    private String generateId() {
        return "NOTIF" + System.currentTimeMillis();
    }
    
    // Getters and Setters
    public String getNotificationId() { return notificationId; }
    public String getUserId() { return userId; }
    public NotificationType getType() { return type; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getRelatedId() { return relatedId; }
    public void setRelatedId(String relatedId) { this.relatedId = relatedId; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}