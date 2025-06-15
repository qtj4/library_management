package edu.epam.fop.dao.impl;

import edu.epam.fop.dao.ConnectionPool;
import edu.epam.fop.dao.UserDao;
import edu.epam.fop.model.User;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UserDaoImpl implements UserDao {

    private static final String INSERT_SQL = "INSERT INTO users (username, password, blocked) VALUES (?, ?, ?)";
    private static final String SELECT_BY_ID_SQL = "SELECT id, username, password, blocked FROM users WHERE id=?";
    private static final String SELECT_BY_USERNAME_SQL = "SELECT id, username, password, blocked FROM users WHERE username=?";
    private static final String SELECT_ALL_SQL = "SELECT id, username, password, blocked FROM users";
    private static final String SELECT_PAGED_SQL = "SELECT id, username, password, blocked FROM users LIMIT ? OFFSET ?";
    private static final String COUNT_SQL = "SELECT COUNT(*) FROM users";
    private static final String UPDATE_SQL = "UPDATE users SET username=?, password=?, blocked=? WHERE id=?";
    private static final String DELETE_SQL = "DELETE FROM users WHERE id=?";

    @Override
    public Long save(User user) throws SQLException {
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setBoolean(3, user.isBlocked());
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
    public User findById(Long id) throws SQLException {
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
    public User findByUsername(String username) throws SQLException {
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_USERNAME_SQL)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<User> findAll() throws SQLException {
        List<User> list = new ArrayList<>();
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
    public void update(User user) throws SQLException {
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setBoolean(3, user.isBlocked());
            ps.setLong(4, user.getId());
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
    public List<User> findPaged(int offset, int limit) throws SQLException {
        List<User> list = new ArrayList<>();
        try(Connection conn=ConnectionPool.getConnection();
            PreparedStatement ps = conn.prepareStatement(SELECT_PAGED_SQL)){
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public long countAll() throws SQLException {
        try(Connection conn=ConnectionPool.getConnection();
            PreparedStatement ps = conn.prepareStatement(COUNT_SQL);
            ResultSet rs = ps.executeQuery()){
            if(rs.next()) return rs.getLong(1);
        }
        return 0;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setBlocked(rs.getBoolean("blocked"));
        return u;
    }
} 