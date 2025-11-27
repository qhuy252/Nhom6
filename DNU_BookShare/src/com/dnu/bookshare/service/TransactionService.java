package src.com.dnu.bookshare.service;

import src.com.dnu.bookshare.model.Transaction;
import src.com.dnu.bookshare.model.Transaction.*;
import src.com.dnu.bookshare.model.Book;
import src.com.dnu.bookshare.persistence.DataManager;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class TransactionService {
    private Map<String, Transaction> transactions;
    private BookService bookService;
    private DataManager dataManager;
    
    public TransactionService(BookService bookService, DataManager dataManager) {
        this.transactions = new HashMap<>();
        this.bookService = bookService;
        this.dataManager = dataManager;
        loadTransactionsFromStorage();
    }
    
    private void loadTransactionsFromStorage() {
        try {
            List<Transaction> loadedTransactions = dataManager.findAllTransactions();
            for (Transaction transaction : loadedTransactions) {
                transactions.put(transaction.getTransactionId(), transaction);
            }
            System.out.println("Đã load " + transactions.size() + " giao dịch");
        } catch (Exception e) {
            System.out.println("Không thể load transactions: " + e.getMessage());
        }
    }
    
    // 29. Tạo yêu cầu mượn/mua
    public Transaction createRequest(String bookId, String borrowerId, 
                                    TransactionType type, String message) throws Exception {
        Book book = bookService.getBookById(bookId);
        if (book == null) {
            throw new Exception("Sách không tồn tại");
        }
        
        if (book.getStatus() != Book.BookStatus.AVAILABLE) {
            throw new Exception("Sách không khả dụng");
        }
        
        if (book.getOwnerId().equals(borrowerId)) {
            throw new Exception("Không thể mượn sách của chính mình");
        }
        
        Transaction transaction = new Transaction(bookId, book.getOwnerId(), 
                                                  borrowerId, type);
        transaction.setMessage(message);
        
        if (type == TransactionType.BUY) {
            transaction.setAmount(book.getPrice());
        }
        
        transactions.put(transaction.getTransactionId(), transaction);
        bookService.updateBookStatus(bookId, Book.BookStatus.RESERVED);
        
        try {
            dataManager.saveTransaction(transaction);
        } catch (Exception e) {
            System.err.println("Lỗi lưu transaction: " + e.getMessage());
        }
        
        return transaction;
    }
    
    // 32. Chấp nhận yêu cầu
    public void approveRequest(String transactionId) throws Exception {
        Transaction transaction = transactions.get(transactionId);
        if (transaction == null) {
            throw new Exception("Giao dịch không tồn tại");
        }
        
        transaction.setStatus(TransactionStatus.APPROVED);
        transaction.setApprovedAt(LocalDateTime.now());
        
        try {
            dataManager.saveTransaction(transaction);
        } catch (Exception e) {
            System.err.println("Lỗi lưu transaction: " + e.getMessage());
        }
    }
    
    // 32. Từ chối yêu cầu
    public void rejectRequest(String transactionId, String reason) throws Exception {
        Transaction transaction = transactions.get(transactionId);
        if (transaction == null) {
            throw new Exception("Giao dịch không tồn tại");
        }
        
        transaction.setStatus(TransactionStatus.REJECTED);
        transaction.setMessage(reason);
        bookService.updateBookStatus(transaction.getBookId(), Book.BookStatus.AVAILABLE);
        
        try {
            dataManager.saveTransaction(transaction);
        } catch (Exception e) {
            System.err.println("Lỗi lưu transaction: " + e.getMessage());
        }
    }
    
    // 33. Xác nhận giao sách
    public void confirmDelivery(String transactionId, int borrowDays) throws Exception {
        Transaction transaction = transactions.get(transactionId);
        if (transaction == null) {
            throw new Exception("Giao dịch không tồn tại");
        }
        
        transaction.setStatus(TransactionStatus.IN_PROGRESS);
        transaction.setDeliveredAt(LocalDateTime.now());
        
        if (transaction.getType() == TransactionType.BORROW) {
            LocalDateTime dueDate = LocalDateTime.now().plusDays(borrowDays);
            transaction.setDueDate(dueDate);
            bookService.updateBookStatus(transaction.getBookId(), Book.BookStatus.BORROWED);
        } else if (transaction.getType() == TransactionType.BUY) {
            bookService.updateBookStatus(transaction.getBookId(), Book.BookStatus.SOLD);
            transaction.setStatus(TransactionStatus.COMPLETED);
        }
        
        try {
            dataManager.saveTransaction(transaction);
        } catch (Exception e) {
            System.err.println("Lỗi lưu transaction: " + e.getMessage());
        }
    }
    
    // 34. Xác nhận trả sách
    public void confirmReturn(String transactionId) throws Exception {
        Transaction transaction = transactions.get(transactionId);
        if (transaction == null) {
            throw new Exception("Giao dịch không tồn tại");
        }
        
        if (transaction.getType() != TransactionType.BORROW) {
            throw new Exception("Chỉ giao dịch mượn mới có trả sách");
        }
        
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setReturnedAt(LocalDateTime.now());
        bookService.updateBookStatus(transaction.getBookId(), Book.BookStatus.AVAILABLE);
        
        try {
            dataManager.saveTransaction(transaction);
        } catch (Exception e) {
            System.err.println("Lỗi lưu transaction: " + e.getMessage());
        }
    }
    
    // 35. Gia hạn mượn sách
    public void extendBorrow(String transactionId, int extraDays) throws Exception {
        Transaction transaction = transactions.get(transactionId);
        if (transaction == null) {
            throw new Exception("Giao dịch không tồn tại");
        }
        
        if (transaction.getType() != TransactionType.BORROW) {
            throw new Exception("Chỉ có thể gia hạn giao dịch mượn");
        }
        
        LocalDateTime newDueDate = transaction.getDueDate().plusDays(extraDays);
        transaction.setDueDate(newDueDate);
        
        try {
            dataManager.saveTransaction(transaction);
        } catch (Exception e) {
            System.err.println("Lỗi lưu transaction: " + e.getMessage());
        }
    }
    
    // 36. Hủy giao dịch
    public void cancelTransaction(String transactionId) throws Exception {
        Transaction transaction = transactions.get(transactionId);
        if (transaction == null) {
            throw new Exception("Giao dịch không tồn tại");
        }
        
        transaction.setStatus(TransactionStatus.CANCELLED);
        
        // Trả lại trạng thái sách
        Book book = bookService.getBookById(transaction.getBookId());
        if (book != null && book.getStatus() != Book.BookStatus.SOLD) {
            bookService.updateBookStatus(transaction.getBookId(), Book.BookStatus.AVAILABLE);
        }
        
        try {
            dataManager.saveTransaction(transaction);
        } catch (Exception e) {
            System.err.println("Lỗi lưu transaction: " + e.getMessage());
        }
    }
    
    // 37. Đánh giá sau giao dịch
    public void rateTransaction(String transactionId, String userId, 
                               int rating, String review) throws Exception {
        Transaction transaction = transactions.get(transactionId);
        if (transaction == null) {
            throw new Exception("Giao dịch không tồn tại");
        }
        
        if (transaction.getStatus() != TransactionStatus.COMPLETED) {
            throw new Exception("Chỉ có thể đánh giá sau khi hoàn thành giao dịch");
        }
        
        if (rating < 1 || rating > 5) {
            throw new Exception("Đánh giá phải từ 1-5 sao");
        }
        
        if (userId.equals(transaction.getOwnerId())) {
            transaction.setBorrowerRating(rating);
            transaction.setBorrowerReview(review);
        } else if (userId.equals(transaction.getBorrowerId())) {
            transaction.setOwnerRating(rating);
            transaction.setOwnerReview(review);
        } else {
            throw new Exception("Bạn không tham gia giao dịch này");
        }
        
        try {
            dataManager.saveTransaction(transaction);
        } catch (Exception e) {
            System.err.println("Lỗi lưu transaction: " + e.getMessage());
        }
    }
    
    // 39-42. Lịch sử giao dịch
    public List<Transaction> getUserTransactions(String userId) {
        return transactions.values().stream()
                .filter(t -> t.getOwnerId().equals(userId) || 
                           t.getBorrowerId().equals(userId))
                .sorted(Comparator.comparing(Transaction::getRequestedAt).reversed())
                .collect(Collectors.toList());
    }
    
    public List<Transaction> getBorrowedBooks(String userId) {
        return transactions.values().stream()
                .filter(t -> t.getBorrowerId().equals(userId) && 
                           t.getType() == TransactionType.BORROW &&
                           t.getStatus() == TransactionStatus.IN_PROGRESS)
                .collect(Collectors.toList());
    }
    
    public List<Transaction> getLentBooks(String userId) {
        return transactions.values().stream()
                .filter(t -> t.getOwnerId().equals(userId) && 
                           t.getType() == TransactionType.BORROW &&
                           t.getStatus() == TransactionStatus.IN_PROGRESS)
                .collect(Collectors.toList());
    }
    
    // 43. Kiểm tra sách quá hạn
    public List<Transaction> getOverdueTransactions() {
        return transactions.values().stream()
                .filter(Transaction::isOverdue)
                .collect(Collectors.toList());
    }
    
    public Transaction getTransactionById(String transactionId) {
        return transactions.get(transactionId);
    }
    
    public List<Transaction> getPendingRequests(String ownerId) {
        return transactions.values().stream()
                .filter(t -> t.getOwnerId().equals(ownerId) && 
                           t.getStatus() == TransactionStatus.PENDING)
                .collect(Collectors.toList());
    }
    
    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactions.values());
    }
    
    /**
     * Lưu tất cả transactions
     */
    public void saveAllTransactions() throws Exception {
        dataManager.saveAllTransactions(new ArrayList<>(transactions.values()));
        System.out.println("Đã lưu tất cả " + transactions.size() + " transactions");
    }
}