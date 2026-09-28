package com.example.assignment_student_att_system.controller;

import com.example.assignment_student_att_system.dao.AttendanceDAO;
import com.example.assignment_student_att_system.dao.StudentDAO;
import com.example.assignment_student_att_system.dao.SubjectDAO;
import com.example.assignment_student_att_system.service.ApiService;
import com.example.assignment_student_att_system.service.AttendanceService;
import com.example.assignment_student_att_system.service.ReportService;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Map;

public class DashboardController {

    @FXML private Label totalStudentsLabel;
    @FXML private Label totalSubjectsLabel;
    @FXML private Label attendanceRateLabel;
    @FXML private Label warningCountLabel;
    @FXML private Label quoteLabel;
    @FXML private ProgressIndicator apiProgress;

    @FXML private PieChart attendancePieChart;
    @FXML private BarChart<String, Number> weeklyBarChart;

    private MainLayoutController mainLayout;
    private final StudentDAO studentDAO = new StudentDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final AttendanceService attendanceService = new AttendanceService();
    private final ApiService apiService = new ApiService();
    private final ReportService reportService = new ReportService();

    @FXML
    public void initialize() {
        loadDashboardMetrics();
        loadPieChartData();
        loadBarChartData();
        loadAsyncApiQuote();
    }

    public void setMainLayout(MainLayoutController mainLayout) {
        this.mainLayout = mainLayout;
    }

    private void loadDashboardMetrics() {
        int totalStudents = studentDAO.getCount();
        int totalSubjects = subjectDAO.getCount();
        double attendancePct = attendanceService.calculateOverallAttendancePercentage();
        int warningCount = attendanceService.getLowAttendanceStudentCount();

        totalStudentsLabel.setText(String.valueOf(totalStudents));
        totalSubjectsLabel.setText(String.valueOf(totalSubjects));
        attendanceRateLabel.setText(String.format("%.1f%%", attendancePct));
        warningCountLabel.setText(String.valueOf(warningCount));
    }

    private void loadPieChartData() {
        attendancePieChart.getData().clear();
        Map<String, Integer> counts = attendanceService.getOverallStatusCounts();
        int present = counts.getOrDefault("PRESENT", 0);
        int absent = counts.getOrDefault("ABSENT", 0);
        int late = counts.getOrDefault("LATE", 0);

        if (present == 0 && absent == 0 && late == 0) {
            attendancePieChart.setTitle("No Attendance Data");
            return;
        }

        PieChart.Data slicePresent = new PieChart.Data("Present (" + present + ")", present);
        PieChart.Data sliceAbsent = new PieChart.Data("Absent (" + absent + ")", absent);
        PieChart.Data sliceLate = new PieChart.Data("Late (" + late + ")", late);

        attendancePieChart.getData().addAll(slicePresent, sliceAbsent, sliceLate);
    }

    private void loadBarChartData() {
        weeklyBarChart.getData().clear();
        Map<String, Map<String, Integer>> trendMap = attendanceDAO.getWeeklyTrendData();

        XYChart.Series<String, Number> presentSeries = new XYChart.Series<>();
        presentSeries.setName("Present");

        XYChart.Series<String, Number> absentSeries = new XYChart.Series<>();
        absentSeries.setName("Absent");

        XYChart.Series<String, Number> lateSeries = new XYChart.Series<>();
        lateSeries.setName("Late");

        for (Map.Entry<String, Map<String, Integer>> entry : trendMap.entrySet()) {
            String date = entry.getKey().substring(5); // Format MM-DD
            Map<String, Integer> statusMap = entry.getValue();

            presentSeries.getData().add(new XYChart.Data<>(date, statusMap.getOrDefault("PRESENT", 0)));
            absentSeries.getData().add(new XYChart.Data<>(date, statusMap.getOrDefault("ABSENT", 0)));
            lateSeries.getData().add(new XYChart.Data<>(date, statusMap.getOrDefault("LATE", 0)));
        }

        weeklyBarChart.getData().addAll(presentSeries, absentSeries, lateSeries);
    }

    @FXML
    public void loadAsyncApiQuote() {
        apiProgress.setVisible(true);
        quoteLabel.setText("Fetching educational tip via Thread Pool HTTP client...");
        apiService.fetchEducationalQuoteAsync(quoteText -> {
            apiProgress.setVisible(false);
            quoteLabel.setText(quoteText);
        });
    }

    @FXML
    public void handleQuickMarkAttendance() {
        if (mainLayout != null) {
            mainLayout.showAttendance();
        }
    }

    @FXML
    public void handleQuickExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Attendance Summary CSV");
        fileChooser.setInitialFileName("attendance_summary.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
        File file = fileChooser.showSaveDialog(quoteLabel.getScene().getWindow());

        if (file != null) {
            boolean success = reportService.exportAllAttendanceToCSV(file);
            Alert alert = new Alert(success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
            alert.setTitle("Export Status");
            alert.setHeaderText(null);
            alert.setContentText(success ? "Attendance exported successfully to " + file.getName() : "Failed to export attendance file.");
            alert.showAndWait();
        }
    }

    @FXML
    public void handleSyncApi() {
        apiProgress.setVisible(true);
        quoteLabel.setText("Syncing attendance records with cloud API server...");
        apiService.syncAttendanceDataAsync(studentDAO.getCount(), success -> {
            apiProgress.setVisible(false);
            loadDashboardMetrics();
            loadPieChartData();
            loadBarChartData();
            quoteLabel.setText("✅ Attendance database successfully synchronized with cloud API server!");
        });
    }
}
