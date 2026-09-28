package com.example.assignment_student_att_system.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainLayoutController {

    @FXML private StackPane contentArea;
    @FXML private Label userLabel;
    @FXML private Label headerTitleLabel;

    @FXML private Button navDashboardBtn;
    @FXML private Button navStudentsBtn;
    @FXML private Button navAttendanceBtn;
    @FXML private Button navReportsBtn;

    private String currentUser = "Administrator";

    @FXML
    public void initialize() {
        showDashboard();
    }

    public void setCurrentUser(String user) {
        this.currentUser = user;
        if (userLabel != null) {
            userLabel.setText(user);
        }
    }

    @FXML
    public void showDashboard() {
        setActiveButton(navDashboardBtn);
        headerTitleLabel.setText("Dashboard Overview");
        loadView("/com/example/assignment_student_att_system/dashboard-view.fxml");
    }

    @FXML
    public void showStudents() {
        setActiveButton(navStudentsBtn);
        headerTitleLabel.setText("Student Management");
        loadView("/com/example/assignment_student_att_system/student-view.fxml");
    }

    @FXML
    public void showAttendance() {
        setActiveButton(navAttendanceBtn);
        headerTitleLabel.setText("Mark & Track Attendance");
        loadView("/com/example/assignment_student_att_system/attendance-view.fxml");
    }

    @FXML
    public void showReports() {
        setActiveButton(navReportsBtn);
        headerTitleLabel.setText("Attendance Reports & Analytics");
        loadView("/com/example/assignment_student_att_system/reports-view.fxml");
    }

    @FXML
    public void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/assignment_student_att_system/login-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle("Student Attendance System - Login");
        } catch (IOException e) {
            System.err.println("Error returning to login: " + e.getMessage());
        }
    }

    private void setActiveButton(Button activeBtn) {
        Button[] buttons = {navDashboardBtn, navStudentsBtn, navAttendanceBtn, navReportsBtn};
        for (Button btn : buttons) {
            if (btn != null) {
                btn.getStyleClass().remove("nav-btn-active");
                if (!btn.getStyleClass().contains("nav-btn")) {
                    btn.getStyleClass().add("nav-btn");
                }
            }
        }
        if (activeBtn != null && !activeBtn.getStyleClass().contains("nav-btn-active")) {
            activeBtn.getStyleClass().add("nav-btn-active");
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node node = loader.load();

            // Pass reference to main layout controller if child controller accepts it
            Object controller = loader.getController();
            if (controller instanceof DashboardController) {
                ((DashboardController) controller).setMainLayout(this);
            }

            contentArea.getChildren().setAll(node);
        } catch (IOException e) {
            System.err.println("Error loading FXML view (" + fxmlPath + "): " + e.getMessage());
            e.printStackTrace();
        }
    }
}
