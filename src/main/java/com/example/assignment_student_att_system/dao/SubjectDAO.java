package com.example.assignment_student_att_system.dao;

import com.example.assignment_student_att_system.database.Database;
import com.example.assignment_student_att_system.model.Subject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SubjectDAO implements CrudDAO<Subject> {

    @Override
    public boolean save(Subject subject) {
        String sql = "INSERT INTO subjects (code, name, department, credits, instructor) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, subject.getCode());
            pstmt.setString(2, subject.getName());
            pstmt.setString(3, subject.getDepartment());
            pstmt.setInt(4, subject.getCredits());
            pstmt.setString(5, subject.getInstructor());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        subject.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error saving subject: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(Subject subject) {
        String sql = "UPDATE subjects SET code = ?, name = ?, department = ?, credits = ?, instructor = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, subject.getCode());
            pstmt.setString(2, subject.getName());
            pstmt.setString(3, subject.getDepartment());
            pstmt.setInt(4, subject.getCredits());
            pstmt.setString(5, subject.getInstructor());
            pstmt.setInt(6, subject.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating subject: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM subjects WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting subject: " + e.getMessage());
        }
        return false;
    }

    @Override
    public Optional<Subject> findById(int id) {
        String sql = "SELECT * FROM subjects WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSubject(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding subject: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Subject> findAll() {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT * FROM subjects ORDER BY code ASC";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToSubject(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all subjects: " + e.getMessage());
        }
        return list;
    }

    public int getCount() {
        String sql = "SELECT COUNT(*) FROM subjects";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error counting subjects: " + e.getMessage());
        }
        return 0;
    }

    private Subject mapResultSetToSubject(ResultSet rs) throws SQLException {
        return new Subject(
                rs.getInt("id"),
                rs.getString("code"),
                rs.getString("name"),
                rs.getString("department"),
                rs.getInt("credits"),
                rs.getString("instructor")
        );
    }
}
