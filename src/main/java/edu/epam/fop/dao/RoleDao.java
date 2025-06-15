package edu.epam.fop.dao;

import edu.epam.fop.model.Role;

import java.sql.SQLException;
import java.util.List;

public interface RoleDao {

    Long save(Role role) throws SQLException;

    Role findById(Long id) throws SQLException;

    Role findByName(String name) throws SQLException;

    List<Role> findAll() throws SQLException;

    // Retrieve roles assigned to specified user via user_roles join table
    List<Role> findByUserId(Long userId) throws SQLException;

    void update(Role role) throws SQLException;

    void deleteById(Long id) throws SQLException;
} 