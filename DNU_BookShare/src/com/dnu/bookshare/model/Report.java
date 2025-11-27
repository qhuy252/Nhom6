// ========== Report.java ==========
package src.com.dnu.bookshare.model;

import java.time.LocalDateTime;

public class Report {
    private String reportId;
    private String reporterId;
    private String reportedUserId;
    private String transactionId;
    private ReportType type;
    private String description;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private String adminNote;
    
    public enum ReportType {
        LATE_RETURN("Trả sách trễ"),
        NOT_RETURN("Không trả sách"),
        DAMAGED_BOOK("Sách bị hư hỏng"),
        FAKE_INFO("Thông tin giả mạo"),
        INAPPROPRIATE("Nội dung không phù hợp"),
        OTHER("Khác");
        
        private String vietnamese;
        ReportType(String vietnamese) { this.vietnamese = vietnamese; }
        public String getVietnamese() { return vietnamese; }
    }
    
    public enum ReportStatus {
        PENDING, INVESTIGATING, RESOLVED, REJECTED
    }
    
    public Report(String reporterId, String reportedUserId, ReportType type, String description) {
        this.reportId = generateId();
        this.reporterId = reporterId;
        this.reportedUserId = reportedUserId;
        this.type = type;
        this.description = description;
        this.status = ReportStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }
    
    private String generateId() {
        return "RPT" + System.currentTimeMillis();
    }
    
    // Getters and Setters
    public String getReportId() { return reportId; }
    public String getReporterId() { return reporterId; }
    public String getReportedUserId() { return reportedUserId; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public ReportType getType() { return type; }
    public String getDescription() { return description; }
    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }
}