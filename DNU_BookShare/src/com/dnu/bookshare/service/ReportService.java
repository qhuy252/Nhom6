package src.com.dnu.bookshare.service;

import src.com.dnu.bookshare.model.Report;
import src.com.dnu.bookshare.model.Report.*;
import src.com.dnu.bookshare.persistence.DataManager;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ReportService {
    private Map<String, Report> reports;
    private DataManager dataManager;
    
    public ReportService(DataManager dataManager) {
        this.reports = new HashMap<>();
        this.dataManager = dataManager;
        loadReportsFromStorage();
    }
    
    private void loadReportsFromStorage() {
        try {
            List<Report> loadedReports = dataManager.findAllReports();
            for (Report report : loadedReports) {
                reports.put(report.getReportId(), report);
            }
            System.out.println("Đã load " + reports.size() + " báo cáo");
        } catch (Exception e) {
            System.out.println("Không thể load reports: " + e.getMessage());
        }
    }
    
    // Tạo báo cáo vi phạm
    public Report createReport(String reporterId, String reportedUserId, 
                              ReportType type, String description, 
                              String transactionId) {
        Report report = new Report(reporterId, reportedUserId, type, description);
        if (transactionId != null) {
            report.setTransactionId(transactionId);
        }
        reports.put(report.getReportId(), report);
        
        try {
            dataManager.saveReport(report);
        } catch (Exception e) {
            System.err.println("Lỗi lưu report: " + e.getMessage());
        }
        
        return report;
    }
    
    // Cập nhật trạng thái báo cáo
    public void updateReportStatus(String reportId, ReportStatus status, 
                                   String adminNote) throws Exception {
        Report report = reports.get(reportId);
        if (report == null) {
            throw new Exception("Báo cáo không tồn tại");
        }
        
        report.setStatus(status);
        report.setAdminNote(adminNote);
        
        if (status == ReportStatus.RESOLVED || status == ReportStatus.REJECTED) {
            report.setResolvedAt(LocalDateTime.now());
        }
        
        try {
            dataManager.saveReport(report);
        } catch (Exception e) {
            System.err.println("Lỗi cập nhật report: " + e.getMessage());
        }
    }
    
    // Lấy báo cáo chờ xử lý
    public List<Report> getPendingReports() {
        return reports.values().stream()
                .filter(r -> r.getStatus() == ReportStatus.PENDING)
                .sorted(Comparator.comparing(Report::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }
    
    // Lấy báo cáo theo người dùng
    public List<Report> getReportsByUser(String userId) {
        return reports.values().stream()
                .filter(r -> r.getReportedUserId().equals(userId))
                .collect(Collectors.toList());
    }
    
    public Report getReportById(String reportId) {
        return reports.get(reportId);
    }
    
    public List<Report> getAllReports() {
        return new ArrayList<>(reports.values());
    }
    
    /**
     * Lưu tất cả reports
     */
    public void saveAllReports() throws Exception {
        dataManager.saveAllReports(new ArrayList<>(reports.values()));
        System.out.println("Đã lưu tất cả " + reports.size() + " reports");
    }
}