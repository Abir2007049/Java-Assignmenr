package com.example.assignment_student_att_system.dao;

import com.example.assignment_student_att_system.database.Database;
import com.example.assignment_student_att_system.model.Attendance;

import java.sql.*;
import java.util.*;

public class AttendanceDAO implements CrudDAO<Attendance> {

    @Override
    public boolean save(Attendance attendance) {
        // Upsert style: replace existing attendance record for same student, subject, and date
        String sql = """
            INSERT INTO attendance (student_id, subject_id, date, status, remarks)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, attendance.getStudentId());
            pstmt.setInt(2, attendance.getSubjectId());
            pstmt.setString(3, attendance.getDate());
            pstmt.setString(4, attendance.getStatus());
            pstmt.setString(5, attendance.getRemarks());

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) attendance.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error saving attendance: " + e.getMessage());
        }
        return false;
    }

    public boolean saveBatch(List<Attendance> list) {
        String deleteSql = "DELETE FROM attendance WHERE subject_id = ? AND date = ? AND student_id = ?";
        String insertSql = "INSERT INTO attendance (student_id, subject_id, date, status, remarks) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement delStmt = conn.prepareStatement(deleteSql);
                 PreparedStatement insStmt = conn.prepareStatement(insertSql)) {
                
                for (Attendance att : list) {
                    // Delete existing record for student + subject + date to prevent duplicate logs
                    delStmt.setInt(1, att.getSubjectId());
                    delStmt.setString(2, att.getDate());
                    delStmt.setString(3, att.getStudentId());
                    delStmt.executeUpdate();

                    insStmt.setString(1, att.getStudentId());
                    insStmt.setInt(2, att.getSubjectId());
                    insStmt.setString(3, att.getDate());
                    insStmt.setString(4, att.getStatus());
                    insStmt.setString(5, att.getRemarks());
                    insStmt.addBatch();
                }
                insStmt.executeBatch();
                conn.commit();
                conn.setAutoCommit(true);
                return true;
            } catch (SQLException e) {
                conn.rollback();
                conn.setAutoCommit(true);
                System.err.println("Error in saveBatch attendance: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("Transaction error in saveBatch: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(Attendance attendance) {
        String sql = "UPDATE attendance SET status = ?, remarks = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, attendance.getStatus());
            pstmt.setString(2, attendance.getRemarks());
            pstmt.setInt(3, attendance.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating attendance: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM attendance WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting attendance: " + e.getMessage());
        }
        return false;
    }

    @Override
    public Optional<Attendance> findById(int id) {
        String sql = """
            SELECT a.*, s.name as student_name, sub.name as subject_name
            FROM attendance a
            JOIN students s ON a.student_id = s.student_id
            JOIN subjects sub ON a.subject_id = sub.id
            WHERE a.id = ?
        """;
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAttendance(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding attendance by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Attendance> findAll() {
        List<Attendance> list = new ArrayList<>();
        String sql = """
            SELECT a.*, s.name as student_name, sub.name as subject_name
            FROM attendance a
            LEFT JOIN students s ON a.student_id = s.student_id
            LEFT JOIN subjects sub ON a.subject_id = sub.id
            ORDER BY a.date DESC, a.id DESC
        """;
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToAttendance(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all attendance: " + e.getMessage());
        }
        return list;
    }

    public List<Attendance> findBySubjectAndDate(int subjectId, String date) {
        List<Attendance> list = new ArrayList<>();
        String sql = """
            SELECT a.*, s.name as student_name, sub.name as subject_name
            FROM attendance a
            LEFT JOIN students s ON a.student_id = s.student_id
            LEFT JOIN subjects sub ON a.subject_id = sub.id
            WHERE a.subject_id = ? AND a.date = ?
            ORDER BY s.name ASC
        """;
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, subjectId);
            pstmt.setString(2, date);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAttendance(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching attendance by subject and date: " + e.getMessage());
        }
        return list;
    }

    public List<Attendance> findByStudentId(String studentId) {
        List<Attendance> list = new ArrayList<>();
        String sql = """
            SELECT a.*, s.name as student_name, sub.name as subject_name
            FROM attendance a
            LEFT JOIN students s ON a.student_id = s.student_id
            LEFT JOIN subjects sub ON a.subject_id = sub.id
            WHERE a.student_id = ?
            ORDER BY a.date DESC
        """;
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAttendance(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching student attendance: " + e.getMessage());
        }
        return list;
    }

    public Map<String, Integer> getOverallStatusCounts() {
        Map<String, Integer> map = new HashMap<>();
        map.put("PRESENT", 0);
        map.put("ABSENT", 0);
        map.put("LATE", 0);

        String sql = "SELECT status, COUNT(*) as cnt FROM attendance GROUP BY status";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("status").toUpperCase(), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            System.err.println("Error counting attendance status: " + e.getMessage());
        }
        return map;
    }

    public Map<String, Map<String, Integer>> getWeeklyTrendData() {
        // Returns date -> status counts
        Map<String, Map<String, Integer>> trend = new LinkedHashMap<>();
        String sql = """
            SELECT date, status, COUNT(*) as count 
            FROM attendance 
            GROUP BY date, status 
            ORDER BY date DESC LIMIT 30
        """;
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String d = rs.getString("date");
                String s = rs.getString("status");
                int c = rs.getInt("count");
                trend.computeIfAbsent(d, k -> new HashMap<>()).put(s, c);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching trend data: " + e.getMessage());
        }
        return trend;
    }

    private Attendance mapResultSetToAttendance(ResultSet rs) throws SQLException {
        return new Attendance(
                rs.getInt("id"),
                rs.getString("student_id"),
                rs.getString("student_name") != null ? rs.getString("student_name") : rs.getString("student_id"),
                rs.getInt("subject_id"),
                rs.getString("subject_name") != null ? rs.getString("subject_name") : "Subject #" + rs.getInt("subject_id"),
                rs.getString("date"),
                rs.getString("status"),
                rs.getString("remarks")
        );
    }
}
