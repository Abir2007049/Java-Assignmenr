package com.example.assignment_student_att_system.report;

import com.example.assignment_student_att_system.model.Attendance;
import com.example.assignment_student_att_system.model.Student;

import java.util.List;

public class StudentAttendanceReport extends Report {
    private Student student;
    private List<Attendance> attendanceList;

    public StudentAttendanceReport(Student student, List<Attendance> attendanceList) {
        super("Student Attendance Summary Report", 
              "Detailed attendance analysis for student: " + (student != null ? student.getName() : "N/A"));
        this.student = student;
        this.attendanceList = attendanceList;
    }

    public Student getStudent() {
        return student;
    }

    public List<Attendance> getAttendanceList() {
        return attendanceList;
    }

    @Override
    public String generateReport() {
        if (student == null) return "No student selected.";

        int total = attendanceList.size();
        long presentCount = attendanceList.stream().filter(a -> "PRESENT".equalsIgnoreCase(a.getStatus())).count();
        long lateCount = attendanceList.stream().filter(a -> "LATE".equalsIgnoreCase(a.getStatus())).count();
        long absentCount = attendanceList.stream().filter(a -> "ABSENT".equalsIgnoreCase(a.getStatus())).count();

        // Count late as 0.5 or present
        double effectivePresent = presentCount + (lateCount * 0.5);
        double percentage = total > 0 ? (effectivePresent / total) * 100.0 : 0.0;

        String eligibility = percentage >= 75.0 ? "ELIGIBLE FOR EXAMINATIONS" : "WARNING: LOW ATTENDANCE (<75%)";

        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append("         STUDENT ATTENDANCE REPORT                  \n");
        sb.append("====================================================\n");
        sb.append("Generated On   : ").append(getGeneratedAt()).append("\n");
        sb.append("Student ID     : ").append(student.getStudentId()).append("\n");
        sb.append("Student Name   : ").append(student.getName()).append("\n");
        sb.append("Email          : ").append(student.getEmail()).append("\n");
        sb.append("Department     : ").append(student.getDepartment()).append("\n");
        sb.append("Status         : ").append(student.getStatus()).append("\n");
        sb.append("----------------------------------------------------\n");
        sb.append("ATTENDANCE STATISTICS:\n");
        sb.append("  Total Classes Scheduled : ").append(total).append("\n");
        sb.append("  Classes Present         : ").append(presentCount).append("\n");
        sb.append("  Classes Late            : ").append(lateCount).append("\n");
        sb.append("  Classes Absent          : ").append(absentCount).append("\n");
        sb.append("  Attendance Rate         : ").append(String.format("%.2f", percentage)).append("%\n");
        sb.append("  Exam Eligibility Status : ").append(eligibility).append("\n");
        sb.append("----------------------------------------------------\n");
        sb.append("DETAILED LOGS:\n");
        sb.append(String.format("%-12s | %-25s | %-10s | %-15s\n", "Date", "Subject", "Status", "Remarks"));
        sb.append("----------------------------------------------------\n");

        if (attendanceList.isEmpty()) {
            sb.append("No attendance records found.\n");
        } else {
            for (Attendance att : attendanceList) {
                sb.append(String.format("%-12s | %-25s | %-10s | %-15s\n",
                        att.getDate(),
                        truncate(att.getSubjectName(), 25),
                        att.getStatus(),
                        truncate(att.getRemarks() != null ? att.getRemarks() : "-", 15)));
            }
        }
        sb.append("====================================================\n");
        return sb.toString();
    }

    public String generateCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append("Date,Student ID,Student Name,Subject,Status,Remarks\n");
        for (Attendance att : attendanceList) {
            sb.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    att.getDate(),
                    student.getStudentId(),
                    student.getName(),
                    att.getSubjectName(),
                    att.getStatus(),
                    att.getRemarks() != null ? att.getRemarks() : ""));
        }
        return sb.toString();
    }

    private String truncate(String text, int length) {
        if (text == null) return "";
        return text.length() > length ? text.substring(0, length - 3) + "..." : text;
    }
}
