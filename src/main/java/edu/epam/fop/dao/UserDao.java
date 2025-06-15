package edu.epam.fop.dao;

import edu.epam.fop.model.User;

import java.sql.SQLException;
import java.util.List;

public interface UserDao {

    Long save(User user) throws SQLException;

    User findById(Long id) throws SQLException;

    User findByUsername(String username) throws SQLException;

    List<User> findAll() throws SQLException;

    List<User> findPaged(int offset, int limit) throws SQLException;

    long countAll() throws SQLException;

    void update(User user) throws SQLException;

    void deleteById(Long id) throws SQLException;
} 