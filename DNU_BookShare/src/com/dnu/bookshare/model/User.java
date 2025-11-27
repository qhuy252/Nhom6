// ========== User.java ==========
package src.com.dnu.bookshare.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class User {
    private String userId;
    private String email;
    private String password;
    private String fullName;
    private String studentId;
    private String phoneNumber;
    private String faculty;
    private UserRole role;
    private double trustScore;
    private LocalDateTime createdAt;
    private boolean isActive;
    private List<String> favoriteBookIds;
    
    public enum UserRole {
        STUDENT, ADMIN
    }
    
    public User(String email, String password, String fullName, String studentId) {
        this.userId = generateId();
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.studentId = studentId;
        this.role = UserRole.STUDENT;
        this.trustScore = 5.0;
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
        this.favoriteBookIds = new ArrayList<>();
    }
    
    
    private String generateId() {
        return "USER" + System.currentTimeMillis();
    }
    
    // Getters and Setters
    public String getUserId() { return userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getStudentId() { return studentId; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getFaculty() { return faculty; }
    public void setFaculty(String faculty) { this.faculty = faculty; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public double getTrustScore() { return trustScore; }
    public void setTrustScore(double trustScore) { this.trustScore = trustScore; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public List<String> getFavoriteBookIds() { return favoriteBookIds; }
    
    public void addFavoriteBook(String bookId) {
        if (!favoriteBookIds.contains(bookId)) {
            favoriteBookIds.add(bookId);
        }
    }
    
    public void removeFavoriteBook(String bookId) {
        favoriteBookIds.remove(bookId);
    }
}