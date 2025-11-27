package src.com.dnu.bookshare.persistence;

import src.com.dnu.bookshare.model.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

/**
 * DataManager - Quản lý lưu trữ dữ liệu vào file JSON
 * Sử dụng Gson để serialize/deserialize objects
 */
public class DataManager {
    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + "/users.json";
    private static final String BOOKS_FILE = DATA_DIR + "/books.json";
    private static final String TRANSACTIONS_FILE = DATA_DIR + "/transactions.json";
    private static final String NOTIFICATIONS_FILE = DATA_DIR + "/notifications.json";
    private static final String REPORTS_FILE = DATA_DIR + "/reports.json";
    
    private Gson gson;
    
    public DataManager() {
        // Khởi tạo Gson với pretty printing
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();
        
        // Tạo thư mục data nếu chưa có
        createDataDirectory();
    }
    
    private void createDataDirectory() {
        try {
            Path dataPath = Paths.get(DATA_DIR);
            if (!Files.exists(dataPath)) {
                Files.createDirectories(dataPath);
                System.out.println("Đã tạo thư mục data");
            }
        } catch (IOException e) {
            System.err.println("Lỗi tạo thư mục data: " + e.getMessage());
        }
    }
    
    // ============ USER OPERATIONS ============
    
    public void saveUser(User user) throws IOException {
        List<User> users = findAllUsers();
        
        // Xóa user cũ nếu đã tồn tại
        users.removeIf(u -> u.getUserId().equals(user.getUserId()));
        
        // Thêm user mới
        users.add(user);
        
        // Lưu lại file
        saveToFile(USERS_FILE, users);
    }
    
    public void saveAllUsers(List<User> users) throws IOException {
        saveToFile(USERS_FILE, users);
    }
    
    public List<User> findAllUsers() {
        try {
            Type listType = new TypeToken<List<User>>(){}.getType();
            return loadFromFile(USERS_FILE, listType);
        } catch (IOException e) {
            System.out.println("Không tìm thấy file users, tạo danh sách mới");
            return new ArrayList<>();
        }
    }
    
    public User findUserById(String userId) {
        return findAllUsers().stream()
                .filter(u -> u.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }
    
    public User findUserByEmail(String email) {
        return findAllUsers().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }
    
    // ============ BOOK OPERATIONS ============
    
    public void saveBook(Book book) throws IOException {
        List<Book> books = findAllBooks();
        books.removeIf(b -> b.getBookId().equals(book.getBookId()));
        books.add(book);
        saveToFile(BOOKS_FILE, books);
    }
    
    public void saveAllBooks(List<Book> books) throws IOException {
        saveToFile(BOOKS_FILE, books);
    }
    
    public List<Book> findAllBooks() {
        try {
            Type listType = new TypeToken<List<Book>>(){}.getType();
            return loadFromFile(BOOKS_FILE, listType);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
    
    public Book findBookById(String bookId) {
        return findAllBooks().stream()
                .filter(b -> b.getBookId().equals(bookId))
                .findFirst()
                .orElse(null);
    }
    
    public void deleteBook(String bookId) throws IOException {
        List<Book> books = findAllBooks();
        books.removeIf(b -> b.getBookId().equals(bookId));
        saveToFile(BOOKS_FILE, books);
    }
    
    // ============ TRANSACTION OPERATIONS ============
    
    public void saveTransaction(Transaction transaction) throws IOException {
        List<Transaction> transactions = findAllTransactions();
        transactions.removeIf(t -> t.getTransactionId().equals(transaction.getTransactionId()));
        transactions.add(transaction);
        saveToFile(TRANSACTIONS_FILE, transactions);
    }
    
    public void saveAllTransactions(List<Transaction> transactions) throws IOException {
        saveToFile(TRANSACTIONS_FILE, transactions);
    }
    
    public List<Transaction> findAllTransactions() {
        try {
            Type listType = new TypeToken<List<Transaction>>(){}.getType();
            return loadFromFile(TRANSACTIONS_FILE, listType);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
    
    public Transaction findTransactionById(String transactionId) {
        return findAllTransactions().stream()
                .filter(t -> t.getTransactionId().equals(transactionId))
                .findFirst()
                .orElse(null);
    }
    
    // ============ NOTIFICATION OPERATIONS ============
    
    public void saveNotification(Notification notification) throws IOException {
        List<Notification> notifications = findAllNotifications();
        notifications.removeIf(n -> n.getNotificationId().equals(notification.getNotificationId()));
        notifications.add(notification);
        saveToFile(NOTIFICATIONS_FILE, notifications);
    }
    
    public void saveAllNotifications(List<Notification> notifications) throws IOException {
        saveToFile(NOTIFICATIONS_FILE, notifications);
    }
    
    public List<Notification> findAllNotifications() {
        try {
            Type listType = new TypeToken<List<Notification>>(){}.getType();
            return loadFromFile(NOTIFICATIONS_FILE, listType);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
    
    public List<Notification> findNotificationsByUser(String userId) {
        return findAllNotifications().stream()
                .filter(n -> n.getUserId().equals(userId))
                .toList();
    }
    
    // ============ REPORT OPERATIONS ============
    
    public void saveReport(Report report) throws IOException {
        List<Report> reports = findAllReports();
        reports.removeIf(r -> r.getReportId().equals(report.getReportId()));
        reports.add(report);
        saveToFile(REPORTS_FILE, reports);
    }
    
    public void saveAllReports(List<Report> reports) throws IOException {
        saveToFile(REPORTS_FILE, reports);
    }
    
    public List<Report> findAllReports() {
        try {
            Type listType = new TypeToken<List<Report>>(){}.getType();
            return loadFromFile(REPORTS_FILE, listType);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
    
    // ============ HELPER METHODS ============
    
    private void saveToFile(String filename, Object data) throws IOException {
        String json = gson.toJson(data);
        Files.writeString(Paths.get(filename), json);
    }
    
    private <T> T loadFromFile(String filename, Type type) throws IOException {
        String json = Files.readString(Paths.get(filename));
        return gson.fromJson(json, type);
    }
    
    /**
     * Xóa tất cả dữ liệu (dùng cho testing)
     */
    public void clearAllData() throws IOException {
        Files.deleteIfExists(Paths.get(USERS_FILE));
        Files.deleteIfExists(Paths.get(BOOKS_FILE));
        Files.deleteIfExists(Paths.get(TRANSACTIONS_FILE));
        Files.deleteIfExists(Paths.get(NOTIFICATIONS_FILE));
        Files.deleteIfExists(Paths.get(REPORTS_FILE));
        System.out.println("Đã xóa tất cả dữ liệu");
    }
    
    /**
     * Backup dữ liệu
     */
    public void backupData() throws IOException {
        String backupDir = DATA_DIR + "/backup_" + System.currentTimeMillis();
        Files.createDirectories(Paths.get(backupDir));
        
        // Copy tất cả file
        if (Files.exists(Paths.get(USERS_FILE))) {
            Files.copy(Paths.get(USERS_FILE), 
                      Paths.get(backupDir + "/users.json"));
        }
        if (Files.exists(Paths.get(BOOKS_FILE))) {
            Files.copy(Paths.get(BOOKS_FILE), 
                      Paths.get(backupDir + "/books.json"));
        }
        if (Files.exists(Paths.get(TRANSACTIONS_FILE))) {
            Files.copy(Paths.get(TRANSACTIONS_FILE), 
                      Paths.get(backupDir + "/transactions.json"));
        }
        if (Files.exists(Paths.get(NOTIFICATIONS_FILE))) {
            Files.copy(Paths.get(NOTIFICATIONS_FILE), 
                      Paths.get(backupDir + "/notifications.json"));
        }
        if (Files.exists(Paths.get(REPORTS_FILE))) {
            Files.copy(Paths.get(REPORTS_FILE), 
                      Paths.get(backupDir + "/reports.json"));
        }
        
        System.out.println("Đã backup dữ liệu vào: " + backupDir);
    }
}