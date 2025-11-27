package src.com.dnu.bookshare.service;

import src.com.dnu.bookshare.model.Book;
import src.com.dnu.bookshare.model.Book.*;
import src.com.dnu.bookshare.persistence.DataManager;
import java.util.*;
import java.util.stream.Collectors;

public class BookService {
    private Map<String, Book> books;
    private DataManager dataManager;
    
    public BookService(DataManager dataManager) {
        this.books = new HashMap<>();
        this.dataManager = dataManager;
        loadBooksFromStorage();
    }
    
    /**
     * Load dữ liệu từ storage
     */
    private void loadBooksFromStorage() {
        try {
            List<Book> loadedBooks = dataManager.findAllBooks();
            for (Book book : loadedBooks) {
                books.put(book.getBookId(), book);
            }
            System.out.println("Đã load " + books.size() + " sách từ storage");
        } catch (Exception e) {
            System.out.println("Không thể load books: " + e.getMessage());
        }
    }
    
    // 9. Đăng tải sách mới
    public Book createBook(String ownerId, String title, String author) {
        Book book = new Book(ownerId, title, author);
        books.put(book.getBookId(), book);
        
        try {
            dataManager.saveBook(book);
            System.out.println("Đã lưu sách: " + title);
        } catch (Exception e) {
            System.err.println("Lỗi lưu sách: " + e.getMessage());
        }
        
        return book;
    }
    
    // 10. Cập nhật thông tin sách
    public void updateBook(String bookId, String title, String author, 
                          String subject, String description) throws Exception {
        Book book = books.get(bookId);
        if (book == null) {
            throw new Exception("Sách không tồn tại");
        }
        
        book.setTitle(title);
        book.setAuthor(author);
        book.setSubject(subject);
        book.setDescription(description);
        
        dataManager.saveBook(book);
    }
    
    // 11. Xóa sách
    public void deleteBook(String bookId, String userId) throws Exception {
        Book book = books.get(bookId);
        if (book == null) {
            throw new Exception("Sách không tồn tại");
        }
        
        if (!book.getOwnerId().equals(userId)) {
            throw new Exception("Bạn không có quyền xóa sách này");
        }
        
        if (book.getStatus() == BookStatus.BORROWED) {
            throw new Exception("Không thể xóa sách đang được mượn");
        }
        
        books.remove(bookId);
        dataManager.deleteBook(bookId);
    }
    
    // 12. Upload ảnh bìa
    public void updateBookImage(String bookId, String imageUrl) {
        Book book = books.get(bookId);
        if (book != null) {
            book.setImageUrl(imageUrl);
            try {
                dataManager.saveBook(book);
            } catch (Exception e) {
                System.err.println("Lỗi lưu ảnh sách: " + e.getMessage());
            }
        }
    }
    
    // 13. Cập nhật tình trạng sách
    public void updateBookCondition(String bookId, BookCondition condition) {
        Book book = books.get(bookId);
        if (book != null) {
            book.setCondition(condition);
            try {
                dataManager.saveBook(book);
            } catch (Exception e) {
                System.err.println("Lỗi cập nhật condition: " + e.getMessage());
            }
        }
    }
    
    // 14. Thêm hình thức giao dịch
    public void addTransactionType(String bookId, TransactionType type, 
                                   Double price, Integer borrowDays) {
        Book book = books.get(bookId);
        if (book != null) {
            book.addTransactionType(type);
            if (type == TransactionType.SELL && price != null) {
                book.setPrice(price);
            }
            if (type == TransactionType.BORROW && borrowDays != null) {
                book.setBorrowDays(borrowDays);
            }
            try {
                dataManager.saveBook(book);
            } catch (Exception e) {
                System.err.println("Lỗi cập nhật transaction type: " + e.getMessage());
            }
        }
    }
    
    // 15. Ẩn/Hiện sách
    public void toggleBookVisibility(String bookId) {
        Book book = books.get(bookId);
        if (book != null) {
            book.setVisible(!book.isVisible());
            try {
                dataManager.saveBook(book);
            } catch (Exception e) {
                System.err.println("Lỗi toggle visibility: " + e.getMessage());
            }
        }
    }
    
    // 16. Tăng lượt xem
    public void incrementViewCount(String bookId) {
        Book book = books.get(bookId);
        if (book != null) {
            book.incrementViewCount();
            try {
                dataManager.saveBook(book);
            } catch (Exception e) {
                System.err.println("Lỗi tăng view count: " + e.getMessage());
            }
        }
    }
    
    // 17. Cập nhật trạng thái sách
    public void updateBookStatus(String bookId, BookStatus status) {
        Book book = books.get(bookId);
        if (book != null) {
            book.setStatus(status);
            try {
                dataManager.saveBook(book);
            } catch (Exception e) {
                System.err.println("Lỗi cập nhật status: " + e.getMessage());
            }
        }
    }
    
    // 21-28. Tìm kiếm và lọc sách
    public List<Book> searchBooks(String keyword, String subject, String faculty,
                                 BookCondition condition, TransactionType type,
                                 String sortBy) {
        List<Book> result = books.values().stream()
                .filter(b -> b.isVisible())
                .collect(Collectors.toList());
        
        // Tìm kiếm theo keyword
        if (keyword != null && !keyword.isEmpty()) {
            String lowerKeyword = keyword.toLowerCase();
            result = result.stream()
                    .filter(b -> b.getTitle().toLowerCase().contains(lowerKeyword) ||
                               b.getAuthor().toLowerCase().contains(lowerKeyword) ||
                               (b.getDescription() != null && 
                                b.getDescription().toLowerCase().contains(lowerKeyword)))
                    .collect(Collectors.toList());
        }
        
        // Lọc theo môn học
        if (subject != null && !subject.isEmpty()) {
            result = result.stream()
                    .filter(b -> subject.equals(b.getSubject()))
                    .collect(Collectors.toList());
        }
        
        // Lọc theo khoa
        if (faculty != null && !faculty.isEmpty()) {
            result = result.stream()
                    .filter(b -> faculty.equals(b.getFaculty()))
                    .collect(Collectors.toList());
        }
        
        // Lọc theo tình trạng
        if (condition != null) {
            result = result.stream()
                    .filter(b -> condition == b.getCondition())
                    .collect(Collectors.toList());
        }
        
        // Lọc theo hình thức
        if (type != null) {
            result = result.stream()
                    .filter(b -> b.getAvailableTypes().contains(type))
                    .collect(Collectors.toList());
        }
        
        // Sắp xếp
        if (sortBy != null) {
            switch (sortBy) {
                case "price_asc":
                    result.sort(Comparator.comparingDouble(Book::getPrice));
                    break;
                case "price_desc":
                    result.sort(Comparator.comparingDouble(Book::getPrice).reversed());
                    break;
                case "newest":
                    result.sort(Comparator.comparing(Book::getBookId).reversed());
                    break;
                case "popular":
                    result.sort(Comparator.comparingInt(Book::getViewCount).reversed());
                    break;
            }
        }
        
        return result;
    }
    
    // Get methods
    public Book getBookById(String bookId) {
        return books.get(bookId);
    }
    
    public List<Book> getBooksByOwner(String ownerId) {
        return books.values().stream()
                .filter(b -> b.getOwnerId().equals(ownerId))
                .collect(Collectors.toList());
    }
    
    public List<Book> getAllBooks() {
        return new ArrayList<>(books.values());
    }
    
    public Map<String, Long> getBookStatsByFaculty() {
        return books.values().stream()
                .filter(b -> b.getFaculty() != null)
                .collect(Collectors.groupingBy(Book::getFaculty, Collectors.counting()));
    }
    
    /**
     * Lưu tất cả sách hiện tại
     */
    public void saveAllBooks() throws Exception {
        dataManager.saveAllBooks(new ArrayList<>(books.values()));
        System.out.println("Đã lưu tất cả " + books.size() + " sách");
    }
}