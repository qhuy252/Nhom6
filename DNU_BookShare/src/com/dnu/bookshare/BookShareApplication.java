package src.com.dnu.bookshare;

import src.com.dnu.bookshare.service.*;
import src.com.dnu.bookshare.model.*;
import src.com.dnu.bookshare.persistence.DataManager;
import java.util.List;
import java.util.Scanner;

/**
 * DNU BookShare Application - Main Application
 * Hệ thống chia sẻ sách sinh viên Đại học Đại Nam
 */
public class BookShareApplication {
    private static DataManager dataManager;
    private static UserService userService;
    private static BookService bookService;
    private static TransactionService transactionService;
    private static ReportService reportService;
    private static NotificationService notificationService;
    private static AdminService adminService;
    
    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("    DNU BOOKSHARE - HỆ THỐNG CHIA SẺ SÁCH    ");
        System.out.println("==============================================\n");
        
        // Khởi tạo các services
        initializeServices();
        
        // Demo các chức năng chính
        try {
            demoApplication();
        } catch (Exception e) {
            System.err.println("Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Khởi tạo tất cả services
     */
    private static void initializeServices() {
        System.out.println("📦 Đang khởi tạo hệ thống...");
        
        // 1. DataManager - quản lý lưu trữ
        dataManager = new DataManager();
        
        // 2. UserService - quản lý người dùng
        userService = new UserService(dataManager);
        
        // 3. BookService - quản lý sách
        bookService = new BookService(dataManager);
        
        // 4. TransactionService - quản lý giao dịch
        transactionService = new TransactionService(bookService, dataManager);
        
        // 5. ReportService - quản lý báo cáo
        reportService = new ReportService(dataManager);
        
        // 6. NotificationService - quản lý thông báo
        notificationService = new NotificationService(dataManager);
        
        // 7. AdminService - quản trị hệ thống
        adminService = new AdminService(
            userService, bookService, transactionService, 
            reportService, notificationService, dataManager
        );
        
        System.out.println("✅ Hệ thống đã sẵn sàng!\n");
    }
    
    /**
     * Demo các chức năng chính của hệ thống
     */
    private static void demoApplication() throws Exception {
        System.out.println("🎯 DEMO CÁC CHỨC NĂNG CHÍNH\n");
        
        // ========== 1. ĐĂNG KÝ VÀ ĐĂNG NHẬP ==========
        System.out.println("1️⃣  ĐĂNG KÝ & ĐĂNG NHẬP");
        System.out.println("─────────────────────────");
        
        // Đăng ký user mới
        User student1 = userService.register(
            "student1@dainam.edu.vn",
            "pass123",
            "Nguyễn Văn A",
            "2021600001"
        );
        student1.setFaculty("CNTT");
        userService.updateProfile(student1.getUserId(), 
            student1.getFullName(), null, "CNTT");
        System.out.println("✓ Đã đăng ký: " + student1.getFullName());
        
        User student2 = userService.register(
            "student2@dainam.edu.vn",
            "pass123",
            "Trần Thị B",
            "2021600002"
        );
        student2.setFaculty("KT");
        userService.updateProfile(student2.getUserId(), 
            student2.getFullName(), null, "KT");
        System.out.println("✓ Đã đăng ký: " + student2.getFullName());
        
        // Đăng nhập
        User loggedIn = userService.login("student1@dainam.edu.vn", "pass123");
        System.out.println("✓ Đăng nhập thành công: " + loggedIn.getFullName() + "\n");
        
        // ========== 2. ĐĂNG SÁCH ==========
        System.out.println("2️⃣  ĐĂNG SÁCH");
        System.out.println("─────────────────────────");
        
        // Student1 đăng sách
        Book book1 = bookService.createBook(
            student1.getUserId(),
            "Lập trình hướng đối tượng với Java",
            "Nguyễn Văn X"
        );
        book1.setSubject("Lập trình Java");
        book1.setFaculty("CNTT");
        book1.setDescription("Sách giáo trình môn OOP, còn mới 95%");
        book1.setCondition(Book.BookCondition.LIKE_NEW);
        book1.addTransactionType(Book.TransactionType.BORROW);
        book1.addTransactionType(Book.TransactionType.SELL);
        bookService.addTransactionType(book1.getBookId(), 
            Book.TransactionType.SELL, 150000.0, null);
        bookService.addTransactionType(book1.getBookId(), 
            Book.TransactionType.BORROW, null, 14);
        System.out.println("✓ Đã đăng sách: " + book1.getTitle());
        
        Book book2 = bookService.createBook(
            student1.getUserId(),
            "Cấu trúc dữ liệu và giải thuật",
            "Trần Văn Y"
        );
        book2.setSubject("Cấu trúc dữ liệu");
        book2.setFaculty("CNTT");
        book2.setCondition(Book.BookCondition.GOOD);
        book2.addTransactionType(Book.TransactionType.BORROW);
        bookService.addTransactionType(book2.getBookId(), 
            Book.TransactionType.BORROW, null, 7);
        System.out.println("✓ Đã đăng sách: " + book2.getTitle());
        
        // Student2 đăng sách
        Book book3 = bookService.createBook(
            student2.getUserId(),
            "Nguyên lý Marketing",
            "Philip Kotler"
        );
        book3.setSubject("Marketing");
        book3.setFaculty("KT");
        book3.setCondition(Book.BookCondition.NEW);
        book3.addTransactionType(Book.TransactionType.SELL);
        bookService.addTransactionType(book3.getBookId(), 
            Book.TransactionType.SELL, 200000.0, null);
        System.out.println("✓ Đã đăng sách: " + book3.getTitle() + "\n");
        
        // ========== 3. TÌM KIẾM SÁCH ==========
        System.out.println("3️⃣  TÌM KIẾM SÁCH");
        System.out.println("─────────────────────────");
        
        List<Book> searchResults = bookService.searchBooks(
            "Java", null, "CNTT", null, null, "newest"
        );
        System.out.println("Tìm thấy " + searchResults.size() + " sách về Java:");
        for (Book book : searchResults) {
            System.out.println("  • " + book.getTitle() + " - " + book.getAuthor());
        }
        System.out.println();
        
        // ========== 4. YÊU CẦU MƯỢN SÁCH ==========
        System.out.println("4️⃣  YÊU CẦU MƯỢN SÁCH");
        System.out.println("─────────────────────────");
        
        // Student2 mượn sách của Student1
        Transaction transaction1 = transactionService.createRequest(
            book1.getBookId(),
            student2.getUserId(),
            Transaction.TransactionType.BORROW,
            "Xin mượn sách để học thi cuối kỳ"
        );
        System.out.println("✓ Đã gửi yêu cầu mượn sách: " + book1.getTitle());
        
        // Tạo thông báo cho chủ sách
        notificationService.notifyNewBorrowRequest(
            student1.getUserId(),
            student2.getFullName(),
            book1.getTitle(),
            transaction1.getTransactionId()
        );
        System.out.println("✓ Đã gửi thông báo cho chủ sách\n");
        
        // ========== 5. XỬ LÝ YÊU CẦU ==========
        System.out.println("5️⃣  XỬ LÝ YÊU CẦU");
        System.out.println("─────────────────────────");
        
        // Xem các yêu cầu đang chờ
        List<Transaction> pendingRequests = transactionService.getPendingRequests(
            student1.getUserId()
        );
        System.out.println("Student1 có " + pendingRequests.size() + " yêu cầu chờ xử lý");
        
        // Chấp nhận yêu cầu
        transactionService.approveRequest(transaction1.getTransactionId());
        System.out.println("✓ Đã chấp nhận yêu cầu");
        
        // Gửi thông báo
        notificationService.notifyRequestApproved(
            student2.getUserId(),
            book1.getTitle(),
            transaction1.getTransactionId()
        );
        System.out.println("✓ Đã gửi thông báo cho người mượn");
        
        // Xác nhận giao sách
        transactionService.confirmDelivery(transaction1.getTransactionId(), 14);
        System.out.println("✓ Đã xác nhận giao sách");
        
        notificationService.notifyBookDelivered(
            student2.getUserId(),
            book1.getTitle(),
            transaction1.getTransactionId()
        );
        System.out.println("✓ Đã gửi thông báo giao sách\n");
        
        // ========== 6. THÔNG BÁO ==========
        System.out.println("6️⃣  THÔNG BÁO");
        System.out.println("─────────────────────────");
        
        // Xem thông báo của Student2
        List<Notification> notifications = notificationService.getUserNotifications(
            student2.getUserId()
        );
        System.out.println("Student2 có " + notifications.size() + " thông báo:");
        for (Notification notif : notifications) {
            String status = notif.isRead() ? "✓" : "●";
            System.out.println("  " + status + " " + notif.getTitle() + 
                             ": " + notif.getMessage());
        }
        
        int unreadCount = notificationService.getUnreadCount(student2.getUserId());
        System.out.println("Chưa đọc: " + unreadCount + " thông báo\n");
        
        // ========== 7. SÁCH ĐANG MƯỢN ==========
        System.out.println("7️⃣  SÁCH ĐANG MƯỢN");
        System.out.println("─────────────────────────");
        
        List<Transaction> borrowedBooks = transactionService.getBorrowedBooks(
            student2.getUserId()
        );
        System.out.println("Student2 đang mượn " + borrowedBooks.size() + " sách:");
        for (Transaction t : borrowedBooks) {
            Book book = bookService.getBookById(t.getBookId());
            System.out.println("  • " + book.getTitle());
            System.out.println("    Hạn trả: " + t.getDueDate());
        }
        System.out.println();
        
        // ========== 8. YÊU THÍCH ==========
        System.out.println("8️⃣  SÁCH YÊU THÍCH");
        System.out.println("─────────────────────────");
        
        // Student2 thêm sách vào yêu thích
        student2.addFavoriteBook(book2.getBookId());
        student2.addFavoriteBook(book3.getBookId());
        userService.updateProfile(student2.getUserId(), 
            student2.getFullName(), null, student2.getFaculty());
        
        System.out.println("Student2 đã thêm " + 
                         student2.getFavoriteBookIds().size() + " sách yêu thích");
        
        // Lấy danh sách sách yêu thích
        for (String bookId : student2.getFavoriteBookIds()) {
            Book book = bookService.getBookById(bookId);
            if (book != null) {
                System.out.println("  ❤️ " + book.getTitle());
            }
        }
        System.out.println();
        
        // ========== 9. ĐÁNH GIÁ ==========
        System.out.println("9️⃣  ĐÁNH GIÁ SAU GIAO DỊCH");
        System.out.println("─────────────────────────");
        
        // Trả sách
        transactionService.confirmReturn(transaction1.getTransactionId());
        System.out.println("✓ Student2 đã trả sách");
        
        notificationService.notifyBookReturned(
            student1.getUserId(),
            student2.getFullName(),
            book1.getTitle(),
            transaction1.getTransactionId()
        );
        
        // Đánh giá
        transactionService.rateTransaction(
            transaction1.getTransactionId(),
            student2.getUserId(),
            5,
            "Sách rất hay, chủ sách nhiệt tình"
        );
        System.out.println("✓ Student2 đã đánh giá 5 sao");
        
        transactionService.rateTransaction(
            transaction1.getTransactionId(),
            student1.getUserId(),
            5,
            "Trả sách đúng hạn, giữ gìn sách tốt"
        );
        System.out.println("✓ Student1 đã đánh giá 5 sao");
        
        // Cập nhật điểm uy tín
        userService.updateTrustScore(student2.getUserId(), 5.0);
        System.out.println("✓ Đã cập nhật điểm uy tín\n");
        
        // ========== 10. BÁO CÁO VI PHẠM ==========
        System.out.println("🔟 BÁO CÁO VI PHẠM");
        System.out.println("─────────────────────────");
        
        // Tạo báo cáo mẫu
        Report report = reportService.createReport(
            student1.getUserId(),
            "USER" + System.currentTimeMillis(),
            Report.ReportType.LATE_RETURN,
            "Trả sách trễ 5 ngày",
            transaction1.getTransactionId()
        );
        System.out.println("✓ Đã tạo báo cáo vi phạm: " + report.getType().getVietnamese());
        
        List<Report> pendingReports = reportService.getPendingReports();
        System.out.println("Có " + pendingReports.size() + " báo cáo chờ xử lý\n");
        
        // ========== 11. QUẢN TRỊ HỆ THỐNG ==========
        System.out.println("1️⃣1️⃣ QUẢN TRỊ HỆ THỐNG");
        System.out.println("─────────────────────────");
        
        // Thống kê hệ thống
        var stats = adminService.getSystemStats();
        System.out.println("📊 THỐNG KÊ HỆ THỐNG:");
        System.out.println("  • Tổng người dùng: " + stats.get("totalUsers"));
        System.out.println("  • Người dùng hoạt động: " + stats.get("activeUsers"));
        System.out.println("  • Tổng sách: " + stats.get("totalBooks"));
        System.out.println("  • Sách đang mượn: " + stats.get("borrowedBooks"));
        System.out.println("  • Tổng giao dịch: " + stats.get("totalTransactions"));
        System.out.println("  • Giao dịch hoàn thành: " + stats.get("completedTransactions"));
        
        // Top sách phổ biến
        System.out.println("\n🔥 TOP SÁCH PHỔ BIẾN:");
        List<Book> topBooks = adminService.getTopPopularBooks(3);
        for (int i = 0; i < topBooks.size(); i++) {
            Book book = topBooks.get(i);
            System.out.println("  " + (i+1) + ". " + book.getTitle() + 
                             " (" + book.getViewCount() + " lượt xem)");
        }
        
        // Gửi thông báo hệ thống
        System.out.println("\n📢 GỬI THÔNG BÁO HỆ THỐNG:");
        adminService.broadcastAnnouncement(
            "Bảo trì hệ thống",
            "Hệ thống sẽ bảo trì vào 2h sáng ngày mai"
        );
        System.out.println("✓ Đã gửi thông báo đến tất cả người dùng");
        
        // Backup dữ liệu
        System.out.println("\n💾 BACKUP DỮ LIỆU:");
        adminService.backupSystemData();
        System.out.println("✓ Đã backup dữ liệu thành công");
        
        System.out.println("\n==============================================");
        System.out.println("           ✅ DEMO HOÀN TẤT!                  ");
        System.out.println("==============================================");
        System.out.println("\n💡 Tất cả dữ liệu đã được lưu vào thư mục 'data/'");
        System.out.println("💡 Dữ liệu sẽ được giữ lại khi khởi động lại ứng dụng");
    }
    
    // Getters for services
    public static DataManager getDataManager() {
        return dataManager;
    }
    
    public static UserService getUserService() {
        return userService;
    }
    
    public static BookService getBookService() {
        return bookService;
    }
    
    public static TransactionService getTransactionService() {
        return transactionService;
    }
    
    public static ReportService getReportService() {
        return reportService;
    }
    
    public static NotificationService getNotificationService() {
        return notificationService;
    }
    
    public static AdminService getAdminService() {
        return adminService;
    }
}