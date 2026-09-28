package com.example.assignment_student_att_system.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class Report {
    private String title;
    private String description;
    private String generatedAt;

    public Report(String title, String description) {
        this.title = title;
        this.description = description;
        this.generatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    // Abstract method to demonstrate OOP Abstraction & Polymorphism
    public abstract String generateReport();

    // Export generated report content to text/CSV file
    public boolean saveToFile(File file, String content) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving report to file: " + e.getMessage());
            return false;
        }
    }
}
