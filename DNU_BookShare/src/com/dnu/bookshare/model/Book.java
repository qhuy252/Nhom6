package src.com.dnu.bookshare.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Book {
    private String bookId;
    private String ownerId;
    private String title;
    private String author;
    private String subject;
    private String faculty;
    private String description;
    private String imageUrl;
    private BookCondition condition;
    private List<TransactionType> availableTypes;
    private double price;
    private int borrowDays;
    private BookStatus status;
    private LocalDateTime postedAt;
    private int viewCount;
    private boolean isVisible;
    
    
    public enum BookCondition {
        NEW("Mới"), LIKE_NEW("Như mới"), GOOD("Tốt"), FAIR("Khá"), OLD("Cũ");
        private String vietnamese;
        BookCondition(String vietnamese) { this.vietnamese = vietnamese; }
        public String getVietnamese() { return vietnamese; }
    }
    
    public enum TransactionType {
        BORROW("Cho mượn"), SELL("Bán"), EXCHANGE("Trao đổi");
        private String vietnamese;
        TransactionType(String vietnamese) { this.vietnamese = vietnamese; }
        public String getVietnamese() { return vietnamese; }
    }
    
    public enum BookStatus {
        AVAILABLE, BORROWED, SOLD, RESERVED
    }
    
    public Book(String ownerId, String title, String author) {
        this.bookId = generateId();
        this.ownerId = ownerId;
        this.title = title;
        this.author = author;
        this.condition = BookCondition.GOOD;
        this.availableTypes = new ArrayList<>();
        this.status = BookStatus.AVAILABLE;
        this.postedAt = LocalDateTime.now();
        this.viewCount = 0;
        this.isVisible = true;
    }
    
    private String generateId() {
        return "BOOK" + System.currentTimeMillis();
    }
    
    // Getters and Setters
    public String getBookId() { return bookId; }
    public String getOwnerId() { return ownerId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getFaculty() { return faculty; }
    public void setFaculty(String faculty) { this.faculty = faculty; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public BookCondition getCondition() { return condition; }
    public void setCondition(BookCondition condition) { this.condition = condition; }
    public List<TransactionType> getAvailableTypes() { return availableTypes; }
    public void addTransactionType(TransactionType type) { 
        if (!availableTypes.contains(type)) {
            availableTypes.add(type);
        }
    }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getBorrowDays() { return borrowDays; }
    public void setBorrowDays(int borrowDays) { this.borrowDays = borrowDays; }
    public BookStatus getStatus() { return status; }
    public void setStatus(BookStatus status) { this.status = status; }
    public int getViewCount() { return viewCount; }
    public void incrementViewCount() { this.viewCount++; }
    public boolean isVisible() { return isVisible; }
    public void setVisible(boolean visible) { isVisible = visible; }
}