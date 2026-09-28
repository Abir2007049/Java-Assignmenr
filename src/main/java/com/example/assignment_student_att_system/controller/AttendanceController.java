package com.example.assignment_student_att_system.controller;

import com.example.assignment_student_att_system.dao.SubjectDAO;
import com.example.assignment_student_att_system.model.Attendance;
import com.example.assignment_student_att_system.model.Subject;
import com.example.assignment_student_att_system.service.AttendanceService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.util.List;

public class AttendanceController {

    @FXML private ComboBox<Subject> subjectComboBox;
    @FXML private DatePicker datePicker;

    @FXML private TableView<Attendance> attendanceTable;
    @FXML private TableColumn<Attendance, String> colStudentId;
    @FXML private TableColumn<Attendance, String> colStudentName;
    @FXML private TableColumn<Attendance, String> colStatus;
    @FXML private TableColumn<Attendance, String> colRemarks;

    @FXML private Label statusSummaryLabel;

    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final AttendanceService attendanceService = new AttendanceService();
    private final ObservableList<Attendance> attendanceList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        datePicker.setValue(LocalDate.now());
        loadSubjects();
        setupTableColumns();

        subjectComboBox.valueProperty().addListener((obs, oldVal, newVal) -> loadAttendanceSheet());
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> loadAttendanceSheet());
    }

    private void loadSubjects() {
        List<Subject> subjects = subjectDAO.findAll();
        subjectComboBox.getItems().setAll(subjects);
        if (!subjects.isEmpty()) {
            subjectComboBox.setValue(subjects.get(0));
        }
    }

    private void setupTableColumns() {
        colStudentId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colStudentName.setCellValueFactory(new PropertyValueFactory<>("studentName"));

        // Status column with choice radio buttons/toggle
        colStatus.setCellFactory(col -> new TableCell<>() {
            private final RadioButton rbPresent = new RadioButton("Present");
            private final RadioButton rbAbsent = new RadioButton("Absent");
            private final RadioButton rbLate = new RadioButton("Late");
            private final ToggleGroup group = new ToggleGroup();
            private final HBox container = new HBox(12, rbPresent, rbAbsent, rbLate);

            {
                rbPresent.setToggleGroup(group);
                rbAbsent.setToggleGroup(group);
                rbLate.setToggleGroup(group);

                rbPresent.setStyle("-fx-text-fill: #34d399; -fx-font-weight: bold;");
                rbAbsent.setStyle("-fx-text-fill: #f87171; -fx-font-weight: bold;");
                rbLate.setStyle("-fx-text-fill: #fbbf24; -fx-font-weight: bold;");

                group.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal != null && getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                        Attendance item = getTableView().getItems().get(getIndex());
                        if (newVal == rbPresent) item.setStatus("PRESENT");
                        else if (newVal == rbAbsent) item.setStatus("ABSENT");
                        else if (newVal == rbLate) item.setStatus("LATE");
                        updateSummaryLabel();
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Attendance att = getTableView().getItems().get(getIndex());
                    String st = att.getStatus();
                    if ("ABSENT".equalsIgnoreCase(st)) {
                        rbAbsent.setSelected(true);
                    } else if ("LATE".equalsIgnoreCase(st)) {
                        rbLate.setSelected(true);
                    } else {
                        rbPresent.setSelected(true);
                    }
                    setGraphic(container);
                }
            }
        });

        // Remarks text box column
        colRemarks.setCellFactory(col -> new TableCell<>() {
            private final TextField textField = new TextField();

            {
                textField.setPromptText("Optional remarks...");
                textField.setStyle("-fx-background-color: #0f172a; -fx-text-fill: #f8fafc; -fx-border-color: #334155;");
                textField.textProperty().addListener((obs, oldVal, newVal) -> {
                    if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                        Attendance att = getTableView().getItems().get(getIndex());
                        att.setRemarks(newVal);
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Attendance att = getTableView().getItems().get(getIndex());
                    textField.setText(att.getRemarks() != null ? att.getRemarks() : "");
                    setGraphic(textField);
                }
            }
        });
    }

    @FXML
    public void loadAttendanceSheet() {
        Subject selectedSubject = subjectComboBox.getValue();
        LocalDate selectedDate = datePicker.getValue();

        if (selectedSubject == null || selectedDate == null) return;

        List<Attendance> list = attendanceService.getAttendanceForSubjectAndDate(selectedSubject.getId(), selectedDate);
        attendanceList.setAll(list);
        attendanceTable.setItems(attendanceList);
        updateSummaryLabel();
    }

    @FXML
    public void markAllPresent() {
        for (Attendance att : attendanceList) {
            att.setStatus("PRESENT");
        }
        attendanceTable.refresh();
        updateSummaryLabel();
    }

    @FXML
    public void handleSaveAttendance() {
        Subject selectedSubject = subjectComboBox.getValue();
        LocalDate selectedDate = datePicker.getValue();

        if (selectedSubject == null || selectedDate == null) {
            showAlert(Alert.AlertType.WARNING, "Please select both a subject and date.");
            return;
        }

        if (attendanceList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No student attendance records to save.");
            return;
        }

        // Set subjectId and date for all rows
        for (Attendance att : attendanceList) {
            att.setSubjectId(selectedSubject.getId());
            att.setDate(selectedDate.toString());
        }

        boolean saved = attendanceService.saveBatchAttendance(attendanceList);
        if (saved) {
            showAlert(Alert.AlertType.INFORMATION, "✅ Attendance recorded successfully for " + selectedSubject.getCode() + " on " + selectedDate + "!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Failed to save attendance records.");
        }
    }

    private void updateSummaryLabel() {
        int present = 0, absent = 0, late = 0;
        for (Attendance att : attendanceList) {
            if ("ABSENT".equalsIgnoreCase(att.getStatus())) absent++;
            else if ("LATE".equalsIgnoreCase(att.getStatus())) late++;
            else present++;
        }
        statusSummaryLabel.setText(String.format("Total: %d  |  Present: %d  |  Absent: %d  |  Late: %d",
                attendanceList.size(), present, absent, late));
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Attendance Notification");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
