package src.com.dnu.bookshare.service;

import src.com.dnu.bookshare.model.*;
import src.com.dnu.bookshare.persistence.DataManager;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AdminService - Quản lý các chức năng admin
 */
public class AdminService {
    private UserService userService;
    private BookService bookService;
    private TransactionService transactionService;
    private ReportService reportService;
    private NotificationService notificationService;
    private DataManager dataManager;
    
    public AdminService(UserService userService, BookService bookService,
                       TransactionService transactionService, ReportService reportService,
                       NotificationService notificationService, DataManager dataManager) {
        this.userService = userService;
        this.bookService = bookService;
        this.transactionService = transactionService;
        this.reportService = reportService;
        this.notificationService = notificationService;
        this.dataManager = dataManager;
    }
    
    // ============ QUẢN LÝ NGƯỜI DÙNG ============
    
    /**
     * Lấy danh sách tất cả người dùng
     */
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
    
    /**
     * Tìm kiếm người dùng
     */
    public List<User> searchUsers(String keyword) {
        return userService.searchUsers(keyword);
    }
    
    /**
     * Khóa tài khoản người dùng
     */
    public void blockUser(String userId, String reason) throws Exception {
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new Exception("Người dùng không tồn tại");
        }
        
        if (user.getRole() == User.UserRole.ADMIN) {
            throw new Exception("Không thể khóa tài khoản admin");
        }
        
        userService.toggleUserStatus(userId, false);
        
        // Thông báo cho user
        notificationService.notifySystemAnnouncement(
            userId,
            "Tài khoản bị khóa",
            "Tài khoản của bạn đã bị khóa. Lý do: " + reason
        );
    }
    
    /**
     * Mở khóa tài khoản
     */
    public void unblockUser(String userId) throws Exception {
        userService.toggleUserStatus(userId, true);
        
        notificationService.notifySystemAnnouncement(
            userId,
            "Tài khoản được mở khóa",
            "Tài khoản của bạn đã được mở khóa. Bạn có thể tiếp tục sử dụng hệ thống."
        );
    }
    
    /**
     * Cập nhật điểm uy tín người dùng
     */
    public void updateUserTrustScore(String userId, double newScore, String reason) {
        userService.updateTrustScore(userId, newScore);
        
        notificationService.notifySystemAnnouncement(
            userId,
            "Điểm uy tín thay đổi",
            "Điểm uy tín của bạn đã được cập nhật. Lý do: " + reason
        );
    }
    
    // ============ QUẢN LÝ SÁCH ============
    
    /**
     * Lấy tất cả sách (bao gồm cả sách ẩn)
     */
    public List<Book> getAllBooksIncludingHidden() {
        return bookService.getAllBooks();
    }
    
    /**
     * Ẩn sách vi phạm
     */
    public void hideBook(String bookId, String reason) throws Exception {
        Book book = bookService.getBookById(bookId);
        if (book == null) {
            throw new Exception("Sách không tồn tại");
        }
        
        bookService.toggleBookVisibility(bookId);
        
        // Thông báo cho chủ sách
        notificationService.notifySystemAnnouncement(
            book.getOwnerId(),
            "Sách bị ẩn",
            "Sách \"" + book.getTitle() + "\" đã bị ẩn. Lý do: " + reason
        );
    }
    
    /**
     * Xóa sách vi phạm
     */
    public void deleteBookByAdmin(String bookId, String reason) throws Exception {
        Book book = bookService.getBookById(bookId);
        if (book == null) {
            throw new Exception("Sách không tồn tại");
        }
        
        String ownerId = book.getOwnerId();
        String title = book.getTitle();
        
        bookService.deleteBook(bookId, ownerId);
        
        // Thông báo cho chủ sách
        notificationService.notifySystemAnnouncement(
            ownerId,
            "Sách bị xóa",
            "Sách \"" + title + "\" đã bị xóa khỏi hệ thống. Lý do: " + reason
        );
    }
    
    // ============ QUẢN LÝ GIAO DỊCH ============
    
    /**
     * Lấy tất cả giao dịch
     */
    public List<Transaction> getAllTransactions() {
        return transactionService.getAllTransactions();
    }
    
    /**
     * Hủy giao dịch
     */
    public void cancelTransaction(String transactionId, String reason) throws Exception {
        Transaction transaction = transactionService.getTransactionById(transactionId);
        if (transaction == null) {
            throw new Exception("Giao dịch không tồn tại");
        }
        
        transactionService.cancelTransaction(transactionId);
        
        // Thông báo cho cả 2 bên
        notificationService.notifySystemAnnouncement(
            transaction.getOwnerId(),
            "Giao dịch bị hủy",
            "Giao dịch đã bị hủy bởi admin. Lý do: " + reason
        );
        
        notificationService.notifySystemAnnouncement(
            transaction.getBorrowerId(),
            "Giao dịch bị hủy",
            "Giao dịch đã bị hủy bởi admin. Lý do: " + reason
        );
    }
    
    // ============ QUẢN LÝ BÁO CÁO VI PHẠM ============
    
    /**
     * Lấy tất cả báo cáo chờ xử lý
     */
    public List<Report> getPendingReports() {
        return reportService.getPendingReports();
    }
    
    /**
     * Xử lý báo cáo
     */
    public void processReport(String reportId, Report.ReportStatus status, 
                             String adminNote) throws Exception {
        Report report = reportService.getReportById(reportId);
        if (report == null) {
            throw new Exception("Báo cáo không tồn tại");
        }
        
        reportService.updateReportStatus(reportId, status, adminNote);
        
        // Thông báo cho người báo cáo
        String message = "Báo cáo của bạn đã được xử lý. ";
        if (status == Report.ReportStatus.RESOLVED) {
            message += "Vi phạm đã được xác nhận và xử lý.";
        } else if (status == Report.ReportStatus.REJECTED) {
            message += "Báo cáo không đủ cơ sở.";
        }
        
        notificationService.notifySystemAnnouncement(
            report.getReporterId(),
            "Báo cáo được xử lý",
            message
        );
        
        // Nếu vi phạm được xác nhận, thông báo cho người bị báo cáo
        if (status == Report.ReportStatus.RESOLVED) {
            notificationService.notifySystemAnnouncement(
                report.getReportedUserId(),
                "Cảnh báo vi phạm",
                "Bạn đã bị báo cáo vi phạm: " + report.getType().getVietnamese()
            );
            
            // Giảm điểm uy tín
            double penalty = 0.5;
            User reportedUser = userService.getUserById(report.getReportedUserId());
            if (reportedUser != null) {
                double newScore = Math.max(0, reportedUser.getTrustScore() - penalty);
                userService.updateTrustScore(report.getReportedUserId(), newScore);
            }
        }
    }
    
    // ============ THỐNG KÊ HỆ THỐNG ============
    
    /**
     * Thống kê tổng quan
     */
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Người dùng
        List<User> allUsers = userService.getAllUsers();
        stats.put("totalUsers", allUsers.size());
        stats.put("activeUsers", allUsers.stream().filter(User::isActive).count());
        stats.put("blockedUsers", allUsers.stream().filter(u -> !u.isActive()).count());
        
        // Sách
        List<Book> allBooks = bookService.getAllBooks();
        stats.put("totalBooks", allBooks.size());
        stats.put("availableBooks", allBooks.stream()
                .filter(b -> b.getStatus() == Book.BookStatus.AVAILABLE).count());
        stats.put("borrowedBooks", allBooks.stream()
                .filter(b -> b.getStatus() == Book.BookStatus.BORROWED).count());
        
        // Giao dịch
        List<Transaction> allTransactions = transactionService.getAllTransactions();
        stats.put("totalTransactions", allTransactions.size());
        stats.put("pendingTransactions", allTransactions.stream()
                .filter(t -> t.getStatus() == Transaction.TransactionStatus.PENDING).count());
        stats.put("completedTransactions", allTransactions.stream()
                .filter(t -> t.getStatus() == Transaction.TransactionStatus.COMPLETED).count());
        
        // Báo cáo
        List<Report> allReports = reportService.getAllReports();
        stats.put("totalReports", allReports.size());
        stats.put("pendingReports", allReports.stream()
                .filter(r -> r.getStatus() == Report.ReportStatus.PENDING).count());
        
        return stats;
    }
    
    /**
     * Thống kê theo khoa
     */
    public Map<String, Long> getStatsByFaculty() {
        return bookService.getBookStatsByFaculty();
    }
    
    /**
     * Top người dùng uy tín
     */
    public List<User> getTopTrustedUsers(int limit) {
        return userService.getAllUsers().stream()
                .sorted(Comparator.comparingDouble(User::getTrustScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Top sách phổ biến
     */
    public List<Book> getTopPopularBooks(int limit) {
        return bookService.getAllBooks().stream()
                .sorted(Comparator.comparingInt(Book::getViewCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    // ============ QUẢN LÝ HỆ THỐNG ============
    
    /**
     * Gửi thông báo hệ thống cho tất cả
     */
    public void broadcastAnnouncement(String title, String message) {
        List<String> allUserIds = userService.getAllUsers().stream()
                .map(User::getUserId)
                .collect(Collectors.toList());
        
        notificationService.broadcastNotification(allUserIds, title, message);
    }
    
    /**
     * Backup dữ liệu
     */
    public void backupSystemData() throws Exception {
        dataManager.backupData();
    }
    
    /**
     * Kiểm tra và xử lý sách quá hạn
     */
    public void checkOverdueBooks() {
        List<Transaction> overdueTransactions = transactionService.getOverdueTransactions();
        
        for (Transaction transaction : overdueTransactions) {
            // Gửi thông báo nhắc nhở
            Book book = bookService.getBookById(transaction.getBookId());
            if (book != null) {
                long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(
                    transaction.getDueDate(), LocalDateTime.now()
                );
                
                notificationService.notifyReturnReminder(
                    transaction.getBorrowerId(),
                    book.getTitle(),
                    (int) -daysOverdue,
                    transaction.getTransactionId()
                );
                
                // Giảm điểm uy tín nếu quá hạn > 3 ngày
                if (daysOverdue > 3) {
                    updateUserTrustScore(
                        transaction.getBorrowerId(),
                        -0.1,
                        "Trả sách quá hạn " + daysOverdue + " ngày"
                    );
                }
            }
        }
    }
}