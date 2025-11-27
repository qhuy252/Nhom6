package src.com.dnu.bookshare.service;

import src.com.dnu.bookshare.model.Notification;
import src.com.dnu.bookshare.model.Notification.*;
import src.com.dnu.bookshare.persistence.DataManager;
import java.util.*;
import java.util.stream.Collectors;

/**
 * NotificationService - Quản lý thông báo cho người dùng
 */
public class NotificationService {
    private Map<String, Notification> notifications;
    private DataManager dataManager;
    
    public NotificationService(DataManager dataManager) {
        this.notifications = new HashMap<>();
        this.dataManager = dataManager;
        loadNotificationsFromStorage();
    }
    
    private void loadNotificationsFromStorage() {
        try {
            List<Notification> loadedNotifs = dataManager.findAllNotifications();
            for (Notification notif : loadedNotifs) {
                notifications.put(notif.getNotificationId(), notif);
            }
            System.out.println("Đã load " + notifications.size() + " thông báo");
        } catch (Exception e) {
            System.out.println("Không thể load notifications: " + e.getMessage());
        }
    }
    
    /**
     * Tạo thông báo mới
     */
    public Notification createNotification(String userId, NotificationType type, 
                                         String title, String message, 
                                         String relatedId) {
        Notification notification = new Notification(userId, type, title, message);
        notification.setRelatedId(relatedId);
        notifications.put(notification.getNotificationId(), notification);
        
        try {
            dataManager.saveNotification(notification);
        } catch (Exception e) {
            System.err.println("Lỗi lưu notification: " + e.getMessage());
        }
        
        return notification;
    }
    
    /**
     * Lấy tất cả thông báo của user
     */
    public List<Notification> getUserNotifications(String userId) {
        return notifications.values().stream()
                .filter(n -> n.getUserId().equals(userId))
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy thông báo chưa đọc
     */
    public List<Notification> getUnreadNotifications(String userId) {
        return notifications.values().stream()
                .filter(n -> n.getUserId().equals(userId) && !n.isRead())
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * Đếm số thông báo chưa đọc
     */
    public int getUnreadCount(String userId) {
        return (int) notifications.values().stream()
                .filter(n -> n.getUserId().equals(userId) && !n.isRead())
                .count();
    }
    
    /**
     * Đánh dấu đã đọc
     */
    public void markAsRead(String notificationId) {
        Notification notification = notifications.get(notificationId);
        if (notification != null) {
            notification.setRead(true);
            try {
                dataManager.saveNotification(notification);
            } catch (Exception e) {
                System.err.println("Lỗi cập nhật notification: " + e.getMessage());
            }
        }
    }
    
    /**
     * Đánh dấu tất cả đã đọc
     */
    public void markAllAsRead(String userId) {
        notifications.values().stream()
                .filter(n -> n.getUserId().equals(userId) && !n.isRead())
                .forEach(n -> {
                    n.setRead(true);
                    try {
                        dataManager.saveNotification(n);
                    } catch (Exception e) {
                        System.err.println("Lỗi cập nhật notification: " + e.getMessage());
                    }
                });
    }
    
    /**
     * Xóa thông báo
     */
    public void deleteNotification(String notificationId) {
        notifications.remove(notificationId);
        // Note: DataManager chưa có deleteNotification, cần implement
    }
    
    /**
     * Xóa tất cả thông báo của user
     */
    public void deleteAllUserNotifications(String userId) {
        notifications.values().removeIf(n -> n.getUserId().equals(userId));
        try {
            dataManager.saveAllNotifications(new ArrayList<>(notifications.values()));
        } catch (Exception e) {
            System.err.println("Lỗi xóa notifications: " + e.getMessage());
        }
    }
    
    // ============ HELPER METHODS - Tạo thông báo cho các sự kiện ============
    
    /**
     * Thông báo có yêu cầu mượn sách mới
     */
    public void notifyNewBorrowRequest(String ownerId, String borrowerName, 
                                      String bookTitle, String transactionId) {
        createNotification(
            ownerId,
            NotificationType.NEW_REQUEST,
            "Yêu cầu mượn sách mới",
            borrowerName + " muốn mượn sách \"" + bookTitle + "\"",
            transactionId
        );
    }
    
    /**
     * Thông báo yêu cầu được chấp nhận
     */
    public void notifyRequestApproved(String borrowerId, String bookTitle, 
                                     String transactionId) {
        createNotification(
            borrowerId,
            NotificationType.REQUEST_APPROVED,
            "Yêu cầu được chấp nhận",
            "Yêu cầu mượn sách \"" + bookTitle + "\" đã được chấp nhận",
            transactionId
        );
    }
    
    /**
     * Thông báo yêu cầu bị từ chối
     */
    public void notifyRequestRejected(String borrowerId, String bookTitle, 
                                     String transactionId) {
        createNotification(
            borrowerId,
            NotificationType.REQUEST_REJECTED,
            "Yêu cầu bị từ chối",
            "Yêu cầu mượn sách \"" + bookTitle + "\" đã bị từ chối",
            transactionId
        );
    }
    
     /**
     * Thông báo sách đã được giao
     */
    public void notifyBookDelivered(String borrowerId, String bookTitle, 
                                   String transactionId) {
        createNotification(
            borrowerId,
            NotificationType.BOOK_DELIVERED,
            "Sách đã được giao",
            "Bạn đã nhận sách \"" + bookTitle + "\". Hãy trả đúng hạn!",
            transactionId
        );
    }
    
    /**
     * Thông báo sách đã được trả
     */
    public void notifyBookReturned(String ownerId, String borrowerName, 
                                  String bookTitle, String transactionId) {
        createNotification(
            ownerId,
            NotificationType.BOOK_RETURNED,
            "Sách đã được trả",
            borrowerName + " đã trả sách \"" + bookTitle + "\"",
            transactionId
        );
    }
    
    /**
     * Nhắc nhở trả sách
     */
    public void notifyReturnReminder(String borrowerId, String bookTitle, 
                                    int daysRemaining, String transactionId) {
        String message;
        if (daysRemaining > 0) {
            message = "Sách \"" + bookTitle + "\" cần trả trong " + daysRemaining + " ngày nữa";
        } else if (daysRemaining == 0) {
            message = "Sách \"" + bookTitle + "\" cần trả hôm nay!";
        } else {
            message = "Sách \"" + bookTitle + "\" đã quá hạn " + (-daysRemaining) + " ngày!";
        }
        
        createNotification(
            borrowerId,
            NotificationType.RETURN_REMINDER,
            "Nhắc nhở trả sách",
            message,
            transactionId
        );
    }
    
    /**
     * Thông báo có tin nhắn mới
     */
    public void notifyNewMessage(String userId, String senderName, String messageId) {
        createNotification(
            userId,
            NotificationType.NEW_MESSAGE,
            "Tin nhắn mới",
            "Bạn có tin nhắn mới từ " + senderName,
            messageId
        );
    }
    
    /**
     * Thông báo có đánh giá mới
     */
    public void notifyNewReview(String userId, String reviewerName, 
                               int rating, String transactionId) {
        createNotification(
            userId,
            NotificationType.NEW_REVIEW,
            "Đánh giá mới",
            reviewerName + " đã đánh giá bạn " + rating + " sao",
            transactionId
        );
    }
    
    /**
     * Thông báo hệ thống
     */
    public void notifySystemAnnouncement(String userId, String title, String message) {
        createNotification(
            userId,
            NotificationType.SYSTEM_ANNOUNCEMENT,
            title,
            message,
            null
        );
    }
    
    /**
     * Gửi thông báo cho tất cả người dùng
     */
    public void broadcastNotification(List<String> userIds, String title, String message) {
        for (String userId : userIds) {
            notifySystemAnnouncement(userId, title, message);
        }
    }
}