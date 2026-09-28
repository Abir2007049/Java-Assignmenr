package com.example.assignment_student_att_system.dao;

import java.util.List;
import java.util.Optional;

public interface CrudDAO<T> {
    boolean save(T entity);
    boolean update(T entity);
    boolean delete(int id);
    Optional<T> findById(int id);
    List<T> findAll();
}
