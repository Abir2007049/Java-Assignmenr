package com.example.assignment_student_att_system.controller;

import com.example.assignment_student_att_system.dao.StudentDAO;
import com.example.assignment_student_att_system.model.Student;
import com.example.assignment_student_att_system.report.StudentAttendanceReport;
import com.example.assignment_student_att_system.service.ReportService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class StudentController {

    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> colStudentId;
    @FXML private TableColumn<Student, String> colName;
    @FXML private TableColumn<Student, String> colEmail;
    @FXML private TableColumn<Student, String> colDept;
    @FXML private TableColumn<Student, String> colStatus;
    @FXML private TableColumn<Student, Void> colActions;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> deptFilterCombo;

    private final StudentDAO studentDAO = new StudentDAO();
    private final ReportService reportService = new ReportService();
    private final ObservableList<Student> studentObservableList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        loadStudentData();
    }

    private void setupTableColumns() {
        colStudentId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colDept.setCellValueFactory(new PropertyValueFactory<>("department"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Status badge custom cell renderer
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    if ("ACTIVE".equalsIgnoreCase(item)) {
                        badge.getStyleClass().add("badge-present");
                    } else {
                        badge.getStyleClass().add("badge-absent");
                    }
                    setGraphic(badge);
                }
            }
        });

        // Action Buttons Column (Edit, Report, Delete)
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button reportBtn = new Button("Report");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(6, editBtn, reportBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("btn-secondary");
                reportBtn.getStyleClass().add("btn-primary");
                deleteBtn.getStyleClass().add("btn-danger");

                editBtn.setOnAction(e -> {
                    Student student = getTableView().getItems().get(getIndex());
                    handleEditStudent(student);
                });

                reportBtn.setOnAction(e -> {
                    Student student = getTableView().getItems().get(getIndex());
                    handleViewReport(student);
                });

                deleteBtn.setOnAction(e -> {
                    Student student = getTableView().getItems().get(getIndex());
                    handleDeleteStudent(student);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void setupFilters() {
        deptFilterCombo.getItems().addAll("All Departments", "Computer Science", "Software Eng", "Electrical Eng");
        deptFilterCombo.setValue("All Departments");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterStudents());
        deptFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> filterStudents());
    }

    private void loadStudentData() {
        studentObservableList.clear();
        studentObservableList.addAll(studentDAO.findAll());
        studentTable.setItems(studentObservableList);
    }

    private void filterStudents() {
        String query = searchField.getText();
        String dept = deptFilterCombo.getValue();
        List<Student> filtered = studentDAO.searchStudents(query, dept);
        studentObservableList.setAll(filtered);
    }

    @FXML
    public void handleAddStudent() {
        Dialog<Student> dialog = new Dialog<>();
        dialog.setTitle("Add New Student");
        dialog.setHeaderText("Enter student details");

        ButtonType saveButtonType = new ButtonType("Save Student", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = createStudentForm(null);
        dialog.getDialogPane().setContent(grid);

        TextField idInput = (TextField) grid.lookup("#studentIdInput");
        TextField nameInput = (TextField) grid.lookup("#nameInput");
        TextField emailInput = (TextField) grid.lookup("#emailInput");
        ComboBox<String> deptInput = (ComboBox<String>) grid.lookup("#deptInput");
        ComboBox<String> statusInput = (ComboBox<String>) grid.lookup("#statusInput");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (idInput.getText().trim().isEmpty() || nameInput.getText().trim().isEmpty()) {
                    showError("Student ID and Name are required!");
                    return null;
                }
                return new Student(
                        idInput.getText().trim(),
                        nameInput.getText().trim(),
                        emailInput.getText().trim(),
                        deptInput.getValue(),
                        statusInput.getValue()
                );
            }
            return null;
        });

        Optional<Student> result = dialog.showAndWait();
        result.ifPresent(student -> {
            if (studentDAO.save(student)) {
                loadStudentData();
            } else {
                showError("Failed to add student. Student ID might already exist.");
            }
        });
    }

    private void handleEditStudent(Student student) {
        Dialog<Student> dialog = new Dialog<>();
        dialog.setTitle("Edit Student");
        dialog.setHeaderText("Modify student information");

        ButtonType saveButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = createStudentForm(student);
        dialog.getDialogPane().setContent(grid);

        TextField idInput = (TextField) grid.lookup("#studentIdInput");
        TextField nameInput = (TextField) grid.lookup("#nameInput");
        TextField emailInput = (TextField) grid.lookup("#emailInput");
        ComboBox<String> deptInput = (ComboBox<String>) grid.lookup("#deptInput");
        ComboBox<String> statusInput = (ComboBox<String>) grid.lookup("#statusInput");

        idInput.setDisable(true); // Don't edit Student ID PK reference

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                student.setName(nameInput.getText().trim());
                student.setEmail(emailInput.getText().trim());
                student.setDepartment(deptInput.getValue());
                student.setStatus(statusInput.getValue());
                return student;
            }
            return null;
        });

        Optional<Student> result = dialog.showAndWait();
        result.ifPresent(updatedStudent -> {
            if (studentDAO.update(updatedStudent)) {
                loadStudentData();
            } else {
                showError("Failed to update student details.");
            }
        });
    }

    private void handleDeleteStudent(Student student) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete " + student.getName() + " (" + student.getStudentId() + ")?");
        alert.setContentText("This will also remove all attendance logs associated with this student.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (studentDAO.delete(student.getId())) {
                loadStudentData();
            } else {
                showError("Failed to delete student.");
            }
        }
    }

    private void handleViewReport(Student student) {
        StudentAttendanceReport report = reportService.generateStudentReport(student.getStudentId());
        if (report == null) {
            showError("Could not generate report for student.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Attendance Summary Report");
        dialog.setHeaderText("Report for " + student.getName());

        TextArea textArea = new TextArea(report.generateReport());
        textArea.setEditable(false);
        textArea.setPrefSize(550, 420);
        textArea.setStyle("-fx-font-family: monospace; -fx-font-size: 13px;");

        ButtonType exportBtn = new ButtonType("Export CSV", ButtonBar.ButtonData.LEFT);
        dialog.getDialogPane().getButtonTypes().addAll(exportBtn, ButtonType.CLOSE);
        dialog.getDialogPane().setContent(textArea);

        dialog.setResultConverter(button -> {
            if (button == exportBtn) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save Student Attendance CSV");
                fileChooser.setInitialFileName(student.getStudentId() + "_report.csv");
                File file = fileChooser.showSaveDialog(searchField.getScene().getWindow());
                if (file != null) {
                    report.saveToFile(file, report.generateCSV());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private GridPane createStudentForm(Student student) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField idInput = new TextField(student != null ? student.getStudentId() : "STU-");
        idInput.setId("studentIdInput");

        TextField nameInput = new TextField(student != null ? student.getName() : "");
        nameInput.setId("nameInput");

        TextField emailInput = new TextField(student != null ? student.getEmail() : "");
        emailInput.setId("emailInput");

        ComboBox<String> deptInput = new ComboBox<>();
        deptInput.getItems().addAll("Computer Science", "Software Eng", "Electrical Eng");
        deptInput.setValue(student != null ? student.getDepartment() : "Computer Science");
        deptInput.setId("deptInput");

        ComboBox<String> statusInput = new ComboBox<>();
        statusInput.getItems().addAll("ACTIVE", "INACTIVE");
        statusInput.setValue(student != null ? student.getStatus() : "ACTIVE");
        statusInput.setId("statusInput");

        grid.add(new Label("Student Roll ID:"), 0, 0);
        grid.add(idInput, 1, 0);
        grid.add(new Label("Full Name:"), 0, 1);
        grid.add(nameInput, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailInput, 1, 2);
        grid.add(new Label("Department:"), 0, 3);
        grid.add(deptInput, 1, 3);
        grid.add(new Label("Status:"), 0, 4);
        grid.add(statusInput, 1, 4);

        return grid;
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
