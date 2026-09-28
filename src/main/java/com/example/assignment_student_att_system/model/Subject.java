package com.example.assignment_student_att_system.model;

import javafx.beans.property.*;

public class Subject {
    private final IntegerProperty id;
    private final StringProperty code;
    private final StringProperty name;
    private final StringProperty department;
    private final IntegerProperty credits;
    private final StringProperty instructor;

    public Subject() {
        this(0, "", "", "", 3, "");
    }

    public Subject(int id, String code, String name, String department, int credits, String instructor) {
        this.id = new SimpleIntegerProperty(id);
        this.code = new SimpleStringProperty(code);
        this.name = new SimpleStringProperty(name);
        this.department = new SimpleStringProperty(department);
        this.credits = new SimpleIntegerProperty(credits);
        this.instructor = new SimpleStringProperty(instructor);
    }

    public Subject(String code, String name, String department, int credits, String instructor) {
        this(0, code, name, department, credits, instructor);
    }

    // Getters and Setters with JavaFX Properties
    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public String getCode() { return code.get(); }
    public void setCode(String value) { code.set(value); }
    public StringProperty codeProperty() { return code; }

    public String getName() { return name.get(); }
    public void setName(String value) { name.set(value); }
    public StringProperty nameProperty() { return name; }

    public String getDepartment() { return department.get(); }
    public void setDepartment(String value) { department.set(value); }
    public StringProperty departmentProperty() { return department; }

    public int getCredits() { return credits.get(); }
    public void setCredits(int value) { credits.set(value); }
    public IntegerProperty creditsProperty() { return credits; }

    public String getInstructor() { return instructor.get(); }
    public void setInstructor(String value) { instructor.set(value); }
    public StringProperty instructorProperty() { return instructor; }

    @Override
    public String toString() {
        return getCode() + " - " + getName();
    }
}
