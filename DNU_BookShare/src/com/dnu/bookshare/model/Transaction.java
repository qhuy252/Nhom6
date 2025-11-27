// ========== Transaction.java ==========
package src.com.dnu.bookshare.model;

import java.time.LocalDateTime;

public class Transaction {
    private String transactionId;
    private String bookId;
    private String ownerId;
    private String borrowerId;
    private TransactionType type;
    private TransactionStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime returnedAt;
    private LocalDateTime dueDate;
    private double amount;
    private String message;
    private int ownerRating;
    private int borrowerRating;
    private String ownerReview;
    private String borrowerReview;
    
    public enum TransactionType {
        BORROW, BUY, EXCHANGE
    }
    
    public enum TransactionStatus {
        PENDING, APPROVED, REJECTED, IN_PROGRESS, COMPLETED, OVERDUE, CANCELLED
    }
    
    public Transaction(String bookId, String ownerId, String borrowerId, TransactionType type) {
        this.transactionId = generateId();
        this.bookId = bookId;
        this.ownerId = ownerId;
        this.borrowerId = borrowerId;
        this.type = type;
        this.status = TransactionStatus.PENDING;
        this.requestedAt = LocalDateTime.now();
    }
    
    private String generateId() {
        return "TXN" + System.currentTimeMillis();
    }
    
    // Getters and Setters
    public String getTransactionId() { return transactionId; }
    public String getBookId() { return bookId; }
    public String getOwnerId() { return ownerId; }
    public String getBorrowerId() { return borrowerId; }
    public TransactionType getType() { return type; }
    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
    public LocalDateTime getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDateTime returnedAt) { this.returnedAt = returnedAt; }
    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public int getOwnerRating() { return ownerRating; }
    public void setOwnerRating(int ownerRating) { this.ownerRating = ownerRating; }
    public int getBorrowerRating() { return borrowerRating; }
    public void setBorrowerRating(int borrowerRating) { this.borrowerRating = borrowerRating; }
    public String getOwnerReview() { return ownerReview; }
    public void setOwnerReview(String ownerReview) { this.ownerReview = ownerReview; }
    public String getBorrowerReview() { return borrowerReview; }
    public void setBorrowerReview(String borrowerReview) { this.borrowerReview = borrowerReview; }
    
    public boolean isOverdue() {
        return dueDate != null && LocalDateTime.now().isAfter(dueDate) && 
               status == TransactionStatus.IN_PROGRESS;
    }
}