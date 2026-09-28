package com.example.assignment_student_att_system.model;

import javafx.beans.property.*;

public class Student {
    private final IntegerProperty id;
    private final StringProperty studentId;
    private final StringProperty name;
    private final StringProperty email;
    private final StringProperty department;
    private final StringProperty status;

    public Student() {
        this(0, "", "", "", "", "ACTIVE");
    }

    public Student(int id, String studentId, String name, String email, String department, String status) {
        this.id = new SimpleIntegerProperty(id);
        this.studentId = new SimpleStringProperty(studentId);
        this.name = new SimpleStringProperty(name);
        this.email = new SimpleStringProperty(email);
        this.department = new SimpleStringProperty(department);
        this.status = new SimpleStringProperty(status);
    }

    public Student(String studentId, String name, String email, String department, String status) {
        this(0, studentId, name, email, department, status);
    }

    // ID Property
    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    // Student ID Property
    public String getStudentId() { return studentId.get(); }
    public void setStudentId(String value) { studentId.set(value); }
    public StringProperty studentIdProperty() { return studentId; }

    // Name Property
    public String getName() { return name.get(); }
    public void setName(String value) { name.set(value); }
    public StringProperty nameProperty() { return name; }

    // Email Property
    public String getEmail() { return email.get(); }
    public void setEmail(String value) { email.set(value); }
    public StringProperty emailProperty() { return email; }

    // Department Property
    public String getDepartment() { return department.get(); }
    public void setDepartment(String value) { department.set(value); }
    public StringProperty departmentProperty() { return department; }

    // Status Property
    public String getStatus() { return status.get(); }
    public void setStatus(String value) { status.set(value); }
    public StringProperty statusProperty() { return status; }

    @Override
    public String toString() {
        return getName() + " (" + getStudentId() + ")";
    }
}
