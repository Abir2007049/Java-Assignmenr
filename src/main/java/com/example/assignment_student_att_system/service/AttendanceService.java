package com.example.assignment_student_att_system.service;

import com.example.assignment_student_att_system.dao.AttendanceDAO;
import com.example.assignment_student_att_system.dao.StudentDAO;
import com.example.assignment_student_att_system.dao.SubjectDAO;
import com.example.assignment_student_att_system.model.Attendance;
import com.example.assignment_student_att_system.model.Student;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AttendanceService {
    private final AttendanceDAO attendanceDAO;
    private final StudentDAO studentDAO;
    private final SubjectDAO subjectDAO;

    public AttendanceService() {
        this.attendanceDAO = new AttendanceDAO();
        this.studentDAO = new StudentDAO();
        this.subjectDAO = new SubjectDAO();
    }

    public List<Attendance> getAttendanceForSubjectAndDate(int subjectId, LocalDate date) {
        String dateStr = date.toString();
        List<Attendance> existing = attendanceDAO.findBySubjectAndDate(subjectId, dateStr);
        
        // If no records exist yet for this date, pre-populate default entries for active students
        if (existing.isEmpty()) {
            List<Student> activeStudents = studentDAO.findAll();
            List<Attendance> freshList = new ArrayList<>();
            for (Student student : activeStudents) {
                if ("ACTIVE".equalsIgnoreCase(student.getStatus())) {
                    freshList.add(new Attendance(
                            0,
                            student.getStudentId(),
                            student.getName(),
                            subjectId,
                            "",
                            dateStr,
                            "PRESENT",
                            ""
                    ));
                }
            }
            return freshList;
        }
        return existing;
    }

    public boolean saveBatchAttendance(List<Attendance> attendanceList) {
        return attendanceDAO.saveBatch(attendanceList);
    }

    public List<Attendance> getAllAttendanceRecords() {
        return attendanceDAO.findAll();
    }

    public List<Attendance> getStudentAttendanceHistory(String studentId) {
        return attendanceDAO.findByStudentId(studentId);
    }

    public Map<String, Integer> getOverallStatusCounts() {
        return attendanceDAO.getOverallStatusCounts();
    }

    public double calculateOverallAttendancePercentage() {
        Map<String, Integer> counts = getOverallStatusCounts();
        int present = counts.getOrDefault("PRESENT", 0);
        int late = counts.getOrDefault("LATE", 0);
        int absent = counts.getOrDefault("ABSENT", 0);
        int total = present + late + absent;

        if (total == 0) return 0.0;
        double effective = present + (late * 0.5);
        return (effective / total) * 100.0;
    }

    public int getLowAttendanceStudentCount() {
        List<Student> students = studentDAO.findAll();
        int warningCount = 0;
        for (Student s : students) {
            List<Attendance> logs = attendanceDAO.findByStudentId(s.getStudentId());
            if (!logs.isEmpty()) {
                long p = logs.stream().filter(a -> "PRESENT".equalsIgnoreCase(a.getStatus())).count();
                long l = logs.stream().filter(a -> "LATE".equalsIgnoreCase(a.getStatus())).count();
                double pct = ((p + (l * 0.5)) / logs.size()) * 100.0;
                if (pct < 75.0) {
                    warningCount++;
                }
            }
        }
        return warningCount;
    }
}
