package edu.epam.fop.dao.impl;

import edu.epam.fop.dao.ConnectionPool;
import edu.epam.fop.dao.RoleDao;
import edu.epam.fop.model.Role;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class RoleDaoImpl implements RoleDao {

    private static final String INSERT_SQL = "INSERT INTO roles (name) VALUES (?)";
    private static final String SELECT_BY_ID_SQL = "SELECT id, name FROM roles WHERE id=?";
    private static final String SELECT_BY_NAME_SQL = "SELECT id, name FROM roles WHERE name=?";
    private static final String SELECT_ALL_SQL = "SELECT id, name FROM roles";
    private static final String UPDATE_SQL = "UPDATE roles SET name=? WHERE id=?";
    private static final String DELETE_SQL = "DELETE FROM roles WHERE id=?";
    private static final String SELECT_BY_USER_SQL = "SELECT r.id, r.name FROM roles r JOIN user_roles ur ON r.id = ur.role_id WHERE ur.user_id=?";

    @Override
    public Long save(Role role) throws SQLException {
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, role.getName());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return null;
    }

    @Override
    public Role findById(Long id) throws SQLException {
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Role findByName(String name) throws SQLException {
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_NAME_SQL)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Role> findAll() throws SQLException {
        List<Role> list = new ArrayList<>();
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public void update(Role role) throws SQLException {
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            ps.setString(1, role.getName());
            ps.setLong(2, role.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void deleteById(Long id) throws SQLException {
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Role> findByUserId(Long userId) throws SQLException {
        List<Role> list = new ArrayList<>();
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_USER_SQL)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private Role mapRow(ResultSet rs) throws SQLException {
        Role r = new Role();
        r.setId(rs.getLong("id"));
        r.setName(rs.getString("name"));
        return r;
    }
} 