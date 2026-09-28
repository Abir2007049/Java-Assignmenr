package com.example.assignment_student_att_system.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:student_attendance.db";
    private static Connection connection = null;

    // Private constructor for Singleton pattern
    private Database() {}

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                System.err.println("SQLite JDBC Driver not found: " + e.getMessage());
            }
            connection = DriverManager.getConnection(DB_URL);
            // Enable Foreign Keys in SQLite
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
        }
        return connection;
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Create Users Table
            String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    full_name TEXT NOT NULL,
                    role TEXT NOT NULL
                );
            """;
            stmt.execute(createUsersTable);

            // 2. Create Students Table
            String createStudentsTable = """
                CREATE TABLE IF NOT EXISTS students (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_id TEXT UNIQUE NOT NULL,
                    name TEXT NOT NULL,
                    email TEXT NOT NULL,
                    department TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'ACTIVE'
                );
            """;
            stmt.execute(createStudentsTable);

            // 3. Create Subjects Table
            String createSubjectsTable = """
                CREATE TABLE IF NOT EXISTS subjects (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    code TEXT UNIQUE NOT NULL,
                    name TEXT NOT NULL,
                    department TEXT NOT NULL,
                    credits INTEGER NOT NULL DEFAULT 3,
                    instructor TEXT NOT NULL
                );
            """;
            stmt.execute(createSubjectsTable);

            // 4. Create Attendance Table
            String createAttendanceTable = """
                CREATE TABLE IF NOT EXISTS attendance (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_id TEXT NOT NULL,
                    subject_id INTEGER NOT NULL,
                    date TEXT NOT NULL,
                    status TEXT NOT NULL,
                    remarks TEXT,
                    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
                    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
                );
            """;
            stmt.execute(createAttendanceTable);

            // Seed sample data if empty
            seedInitialData(conn, stmt);

            System.out.println("Database initialized successfully!");
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void seedInitialData(Connection conn, Statement stmt) throws SQLException {
        // Check if users exist
        var rsUser = stmt.executeQuery("SELECT COUNT(*) FROM users;");
        if (rsUser.next() && rsUser.getInt(1) == 0) {
            stmt.execute("""
                INSERT INTO users (username, password, full_name, role) VALUES 
                ('admin', 'admin123', 'Administrator', 'ADMIN'),
                ('teacher', 'teacher123', 'Prof. Sarah Jenkins', 'TEACHER');
            """);
        }

        // Check if students exist
        var rsStudent = stmt.executeQuery("SELECT COUNT(*) FROM students;");
        if (rsStudent.next() && rsStudent.getInt(1) == 0) {
            stmt.execute("""
                INSERT INTO students (student_id, name, email, department, status) VALUES 
                ('STU-1001', 'Abir Hossain', 'abir@university.edu', 'Computer Science', 'ACTIVE'),
                ('STU-1002', 'Sophia Martinez', 'sophia.m@university.edu', 'Computer Science', 'ACTIVE'),
                ('STU-1003', 'Liam Johnson', 'liam.j@university.edu', 'Electrical Eng', 'ACTIVE'),
                ('STU-1004', 'Emma Watson', 'emma.w@university.edu', 'Software Eng', 'ACTIVE'),
                ('STU-1005', 'Noah Davis', 'noah.d@university.edu', 'Computer Science', 'ACTIVE'),
                ('STU-1006', 'Olivia Taylor', 'olivia.t@university.edu', 'Electrical Eng', 'INACTIVE'),
                ('STU-1007', 'Ethan Brown', 'ethan.b@university.edu', 'Software Eng', 'ACTIVE'),
                ('STU-1008', 'Ava Wilson', 'ava.w@university.edu', 'Computer Science', 'ACTIVE');
            """);
        }

        // Check if subjects exist
        var rsSubject = stmt.executeQuery("SELECT COUNT(*) FROM subjects;");
        if (rsSubject.next() && rsSubject.getInt(1) == 0) {
            stmt.execute("""
                INSERT INTO subjects (code, name, department, credits, instructor) VALUES 
                ('CSE-201', 'Object-Oriented Programming', 'Computer Science', 3, 'Dr. Alan Turing'),
                ('CSE-305', 'Database Management Systems', 'Computer Science', 4, 'Prof. Grace Hopper'),
                ('SWE-102', 'Software Engineering Fundamentals', 'Software Eng', 3, 'Dr. Margaret Hamilton'),
                ('EEE-110', 'Circuit Analysis & Electronics', 'Electrical Eng', 3, 'Prof. Nikola Tesla');
            """);
        }

        // Check if attendance exists
        var rsAttendance = stmt.executeQuery("SELECT COUNT(*) FROM attendance;");
        if (rsAttendance.next() && rsAttendance.getInt(1) == 0) {
            stmt.execute("""
                INSERT INTO attendance (student_id, subject_id, date, status, remarks) VALUES 
                ('STU-1001', 1, '2026-09-22', 'PRESENT', 'On time'),
                ('STU-1001', 1, '2026-09-23', 'PRESENT', 'On time'),
                ('STU-1001', 1, '2026-09-24', 'PRESENT', 'On time'),
                ('STU-1001', 1, '2026-09-25', 'PRESENT', 'On time'),
                ('STU-1001', 1, '2026-09-26', 'PRESENT', 'On time'),
                ('STU-1002', 1, '2026-09-22', 'PRESENT', 'On time'),
                ('STU-1002', 1, '2026-09-23', 'ABSENT', 'Medical leave'),
                ('STU-1002', 1, '2026-09-24', 'PRESENT', 'On time'),
                ('STU-1002', 1, '2026-09-25', 'LATE', '10 mins late'),
                ('STU-1002', 1, '2026-09-26', 'PRESENT', 'On time'),
                ('STU-1003', 1, '2026-09-22', 'ABSENT', 'Unexcused'),
                ('STU-1003', 1, '2026-09-23', 'ABSENT', 'Unexcused'),
                ('STU-1003', 1, '2026-09-24', 'LATE', 'Traffic delay'),
                ('STU-1003', 1, '2026-09-25', 'ABSENT', 'Unexcused'),
                ('STU-1004', 1, '2026-09-22', 'PRESENT', 'On time'),
                ('STU-1004', 1, '2026-09-23', 'PRESENT', 'On time'),
                ('STU-1004', 1, '2026-09-24', 'PRESENT', 'On time'),
                ('STU-1005', 1, '2026-09-22', 'PRESENT', 'On time'),
                ('STU-1005', 1, '2026-09-23', 'PRESENT', 'On time'),
                ('STU-1005', 1, '2026-09-24', 'LATE', 'On time');
            """);
        }
    }
}
