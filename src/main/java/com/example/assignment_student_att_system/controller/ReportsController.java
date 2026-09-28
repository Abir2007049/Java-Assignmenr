package com.example.assignment_student_att_system.controller;

import com.example.assignment_student_att_system.dao.StudentDAO;
import com.example.assignment_student_att_system.dao.SubjectDAO;
import com.example.assignment_student_att_system.model.Student;
import com.example.assignment_student_att_system.model.Subject;
import com.example.assignment_student_att_system.report.StudentAttendanceReport;
import com.example.assignment_student_att_system.service.ReportService;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

public class ReportsController {

    @FXML private ComboBox<Student> studentComboBox;
    @FXML private ComboBox<Subject> subjectComboBox;
    @FXML private TextArea reportPreviewArea;

    private final StudentDAO studentDAO = new StudentDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final ReportService reportService = new ReportService();
    private StudentAttendanceReport currentReport = null;

    @FXML
    public void initialize() {
        List<Student> students = studentDAO.findAll();
        studentComboBox.getItems().setAll(students);
        if (!students.isEmpty()) {
            studentComboBox.setValue(students.get(0));
        }

        List<Subject> subjects = subjectDAO.findAll();
        subjectComboBox.getItems().setAll(subjects);
        if (!subjects.isEmpty()) {
            subjectComboBox.setValue(subjects.get(0));
        }

        generateReport();
    }

    @FXML
    public void generateReport() {
        Student selectedStudent = studentComboBox.getValue();
        if (selectedStudent != null) {
            currentReport = reportService.generateStudentReport(selectedStudent.getStudentId());
            if (currentReport != null) {
                reportPreviewArea.setText(currentReport.generateReport());
            } else {
                reportPreviewArea.setText("No data available for student.");
            }
        }
    }

    @FXML
    public void handleExportCSV() {
        if (currentReport == null) {
            showAlert(Alert.AlertType.WARNING, "Please generate a report first.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Student Attendance CSV");
        fileChooser.setInitialFileName(currentReport.getStudent().getStudentId() + "_attendance.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
        File file = fileChooser.showSaveDialog(reportPreviewArea.getScene().getWindow());

        if (file != null) {
            boolean success = currentReport.saveToFile(file, currentReport.generateCSV());
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Report saved to " + file.getAbsolutePath());
            } else {
                showAlert(Alert.AlertType.ERROR, "Failed to save CSV file.");
            }
        }
    }

    @FXML
    public void handleExportTXT() {
        if (currentReport == null) {
            showAlert(Alert.AlertType.WARNING, "Please generate a report first.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Formatted Report Text");
        fileChooser.setInitialFileName(currentReport.getStudent().getStudentId() + "_report.txt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt"));
        File file = fileChooser.showSaveDialog(reportPreviewArea.getScene().getWindow());

        if (file != null) {
            boolean success = currentReport.saveToFile(file, currentReport.generateReport());
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Text report saved to " + file.getAbsolutePath());
            } else {
                showAlert(Alert.AlertType.ERROR, "Failed to save text report.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Report Export");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
