package com.example.assignment_student_att_system.model;

import javafx.beans.property.*;

public class Attendance {
    private final IntegerProperty id;
    private final StringProperty studentId;
    private final StringProperty studentName;
    private final IntegerProperty subjectId;
    private final StringProperty subjectName;
    private final StringProperty date;
    private final StringProperty status; // PRESENT, ABSENT, LATE
    private final StringProperty remarks;

    public Attendance() {
        this(0, "", "", 0, "", "", "PRESENT", "");
    }

    public Attendance(int id, String studentId, String studentName, int subjectId, String subjectName, String date, String status, String remarks) {
        this.id = new SimpleIntegerProperty(id);
        this.studentId = new SimpleStringProperty(studentId);
        this.studentName = new SimpleStringProperty(studentName);
        this.subjectId = new SimpleIntegerProperty(subjectId);
        this.subjectName = new SimpleStringProperty(subjectName);
        this.date = new SimpleStringProperty(date);
        this.status = new SimpleStringProperty(status);
        this.remarks = new SimpleStringProperty(remarks);
    }

    public Attendance(String studentId, int subjectId, String date, String status, String remarks) {
        this(0, studentId, "", subjectId, "", date, status, remarks);
    }

    // Properties
    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public String getStudentId() { return studentId.get(); }
    public void setStudentId(String value) { studentId.set(value); }
    public StringProperty studentIdProperty() { return studentId; }

    public String getStudentName() { return studentName.get(); }
    public void setStudentName(String value) { studentName.set(value); }
    public StringProperty studentNameProperty() { return studentName; }

    public int getSubjectId() { return subjectId.get(); }
    public void setSubjectId(int value) { subjectId.set(value); }
    public IntegerProperty subjectIdProperty() { return subjectId; }

    public String getSubjectName() { return subjectName.get(); }
    public void setSubjectName(String value) { subjectName.set(value); }
    public StringProperty subjectNameProperty() { return subjectName; }

    public String getDate() { return date.get(); }
    public void setDate(String value) { date.set(value); }
    public StringProperty dateProperty() { return date; }

    public String getStatus() { return status.get(); }
    public void setStatus(String value) { status.set(value); }
    public StringProperty statusProperty() { return status; }

    public String getRemarks() { return remarks.get(); }
    public void setRemarks(String value) { remarks.set(value); }
    public StringProperty remarksProperty() { return remarks; }
}
