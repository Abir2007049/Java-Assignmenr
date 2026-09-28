package com.example.assignment_student_att_system.service;

import com.example.assignment_student_att_system.dao.AttendanceDAO;
import com.example.assignment_student_att_system.dao.StudentDAO;
import com.example.assignment_student_att_system.model.Attendance;
import com.example.assignment_student_att_system.model.Student;
import com.example.assignment_student_att_system.report.StudentAttendanceReport;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ReportService {
    private final StudentDAO studentDAO;
    private final AttendanceDAO attendanceDAO;

    public ReportService() {
        this.studentDAO = new StudentDAO();
        this.attendanceDAO = new AttendanceDAO();
    }

    public StudentAttendanceReport generateStudentReport(String studentId) {
        Optional<Student> studentOpt = studentDAO.findByStudentId(studentId);
        if (studentOpt.isPresent()) {
            Student s = studentOpt.get();
            List<Attendance> logs = attendanceDAO.findByStudentId(studentId);
            return new StudentAttendanceReport(s, logs);
        }
        return null;
    }

    public boolean exportAllAttendanceToCSV(File file) {
        List<Attendance> all = attendanceDAO.findAll();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("ID,Student ID,Student Name,Subject,Date,Status,Remarks\n");
            for (Attendance a : all) {
                writer.write(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        a.getId(),
                        a.getStudentId(),
                        a.getStudentName(),
                        a.getSubjectName(),
                        a.getDate(),
                        a.getStatus(),
                        a.getRemarks() != null ? a.getRemarks() : ""));
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error exporting all attendance CSV: " + e.getMessage());
            return false;
        }
    }
}
