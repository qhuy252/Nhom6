package src.com.dnu.bookshare.service;

import src.com.dnu.bookshare.model.User;
import src.com.dnu.bookshare.persistence.DataManager;

import java.util.*;
import java.util.stream.Collectors;

public class UserService {
    private Map<String, User> users;
    private User currentUser;
    private DataManager dataManager;

    // ✅ Constructor chính xác (không gọi đệ quy)
    public UserService(DataManager dataManager) {
        this.users = new HashMap<>();
        this.dataManager = dataManager;
        loadUsersFromStorage();
        initializeAdminAccount();
    }

    /**
     * Load dữ liệu từ storage
     */
    private void loadUsersFromStorage() {
        try {
            List<User> loadedUsers = dataManager.findAllUsers();
            for (User user : loadedUsers) {
                users.put(user.getUserId(), user);
            }
            System.out.println("Đã load " + users.size() + " người dùng từ storage");
        } catch (Exception e) {
            System.out.println("Không thể load users: " + e.getMessage());
        }
    }

    private void initializeAdminAccount() {
        // Kiểm tra xem đã có admin chưa
        boolean hasAdmin = users.values().stream()
                .anyMatch(u -> u.getRole() == User.UserRole.ADMIN);

        if (!hasAdmin) {
            User admin = new User("admin@dainam.edu.vn", "admin123", "Admin DNU", "ADMIN001");
            admin.setRole(User.UserRole.ADMIN);
            users.put(admin.getUserId(), admin);

            try {
                dataManager.saveUser(admin);
                System.out.println("Đã tạo tài khoản admin mặc định");
            } catch (Exception e) {
                System.err.println("Lỗi lưu admin: " + e.getMessage());
            }
        }
    }

    // 1. Đăng ký tài khoản
    public User register(String email, String password, String fullName, String studentId)
            throws Exception {
        if (!email.endsWith("@dainam.edu.vn")) {
            throw new Exception("Email phải là email sinh viên DNU");
        }

        if (isEmailExists(email)) {
            throw new Exception("Email đã tồn tại");
        }

        if (isStudentIdExists(studentId)) {
            throw new Exception("Mã sinh viên đã tồn tại");
        }

        User newUser = new User(email, password, fullName, studentId);
        users.put(newUser.getUserId(), newUser);

        // Lưu vào storage
        dataManager.saveUser(newUser);
        System.out.println("Đã đăng ký và lưu user: " + email);

        return newUser;
    }

    // 2. Đăng nhập
    public User login(String email, String password) throws Exception {
        User user = users.values().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElse(null);

        if (user == null) {
            throw new Exception("Email không tồn tại");
        }

        if (!user.getPassword().equals(password)) {
            throw new Exception("Mật khẩu không chính xác");
        }

        if (!user.isActive()) {
            throw new Exception("Tài khoản đã bị khóa");
        }

        currentUser = user;
        return user;
    }

    // 3. Đăng xuất
    public void logout() {
        currentUser = null;
    }

    // 4. Cập nhật hồ sơ
    public void updateProfile(String userId, String fullName, String phoneNumber, String faculty)
            throws Exception {
        User user = users.get(userId);
        if (user == null) {
            throw new Exception("Người dùng không tồn tại");
        }

        user.setFullName(fullName);
        user.setPhoneNumber(phoneNumber);
        user.setFaculty(faculty);

        // Lưu vào storage
        dataManager.saveUser(user);
        System.out.println("Đã cập nhật hồ sơ user: " + userId);
    }

    // 5. Đổi mật khẩu
    public void changePassword(String userId, String oldPassword, String newPassword)
            throws Exception {
        User user = users.get(userId);
        if (user == null) {
            throw new Exception("Người dùng không tồn tại");
        }

        if (!user.getPassword().equals(oldPassword)) {
            throw new Exception("Mật khẩu cũ không chính xác");
        }

        user.setPassword(newPassword);
        dataManager.saveUser(user);
        System.out.println("Đã đổi mật khẩu user: " + userId);
    }

    // 6. Quên mật khẩu
    public String resetPassword(String email) throws Exception {
        User user = users.values().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElse(null);

        if (user == null) {
            throw new Exception("Email không tồn tại");
        }

        String newPassword = generateRandomPassword();
        user.setPassword(newPassword);
        dataManager.saveUser(user);
        System.out.println("Đã reset mật khẩu user: " + email);

        return newPassword;
    }

    // 7. Cập nhật điểm uy tín
    public void updateTrustScore(String userId, double score) {
        User user = users.get(userId);
        if (user != null) {
            double currentScore = user.getTrustScore();
            double newScore = (currentScore + score) / 2;
            user.setTrustScore(Math.max(0, Math.min(5, newScore)));

            try {
                dataManager.saveUser(user);
            } catch (Exception e) {
                System.err.println("Lỗi lưu trust score: " + e.getMessage());
            }
        }
    }

    // 8. Khóa / mở khóa tài khoản (admin)
    public void toggleUserStatus(String userId, boolean isActive) throws Exception {
        if (currentUser == null || currentUser.getRole() != User.UserRole.ADMIN) {
            throw new Exception("Bạn không có quyền thực hiện hành động này");
        }

        User user = users.get(userId);
        if (user != null) {
            user.setActive(isActive);
            dataManager.saveUser(user);
            System.out.println("Đã " + (isActive ? "mở khóa" : "khóa") + " user: " + userId);
        }
    }

    // 9. Lưu tất cả users
    public void saveAllUsers() throws Exception {
        dataManager.saveAllUsers(new ArrayList<>(users.values()));
        System.out.println("Đã lưu tất cả " + users.size() + " users");
    }

    // ===== Helper methods =====
    public User getCurrentUser() {
        return currentUser;
    }

    public User getUserById(String userId) {
        return users.get(userId);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public List<User> searchUsers(String keyword) {
        return users.values().stream()
                .filter(u -> u.getFullName().toLowerCase().contains(keyword.toLowerCase())
                        || u.getEmail().toLowerCase().contains(keyword.toLowerCase())
                        || u.getStudentId().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    private boolean isEmailExists(String email) {
        return users.values().stream().anyMatch(u -> u.getEmail().equals(email));
    }

    private boolean isStudentIdExists(String studentId) {
        return users.values().stream().anyMatch(u -> u.getStudentId().equals(studentId));
    }

    private String generateRandomPassword() {
        return "DNU" + (int) (Math.random() * 900000 + 100000);
    }
}
