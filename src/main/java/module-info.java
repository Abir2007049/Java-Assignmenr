module com.example.assignment_student_att_system {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.net.http;
    requires org.json;

    opens com.example.assignment_student_att_system to javafx.fxml;
    opens com.example.assignment_student_att_system.controller to javafx.fxml;
    opens com.example.assignment_student_att_system.model to javafx.base, javafx.fxml;

    exports com.example.assignment_student_att_system;
    exports com.example.assignment_student_att_system.controller;
    exports com.example.assignment_student_att_system.model;
    exports com.example.assignment_student_att_system.dao;
    exports com.example.assignment_student_att_system.service;
    exports com.example.assignment_student_att_system.database;
    exports com.example.assignment_student_att_system.report;
}