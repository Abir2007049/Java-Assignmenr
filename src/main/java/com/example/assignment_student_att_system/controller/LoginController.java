package com.example.assignment_student_att_system.controller;

import com.example.assignment_student_att_system.database.Database;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private ComboBox<String> roleComboBox;

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("Admin", "Teacher");
        roleComboBox.setValue("Admin");
    }

    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT * FROM users WHERE username = ? AND password = ?")) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String fullName = rs.getString("full_name");
                    openMainDashboard(fullName);
                } else {
                    showError("Invalid credentials. Try admin / admin123");
                }
            }
        } catch (Exception e) {
            showError("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void fillAdminDemo() {
        usernameField.setText("admin");
        passwordField.setText("admin123");
        roleComboBox.setValue("Admin");
        errorLabel.setVisible(false);
    }

    @FXML
    public void fillTeacherDemo() {
        usernameField.setText("teacher");
        passwordField.setText("teacher123");
        roleComboBox.setValue("Teacher");
        errorLabel.setVisible(false);
    }

    private void openMainDashboard(String userFullName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/assignment_student_att_system/main-layout.fxml"));
            Parent root = loader.load();

            MainLayoutController controller = loader.getController();
            controller.setCurrentUser(userFullName);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 720));
            stage.setTitle("Student Attendance Management System");
            stage.setMinWidth(950);
            stage.setMinHeight(650);
        } catch (Exception e) {
            showError("Error launching dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}
