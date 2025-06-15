package edu.epam.fop.dao.impl;

import edu.epam.fop.dao.ConnectionPool;
import edu.epam.fop.dao.OrderDao;
import edu.epam.fop.model.Order;
import edu.epam.fop.model.OrderStatus;
import edu.epam.fop.model.LendingType;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OrderDaoImpl implements OrderDao {

    private static final String INSERT_SQL = "INSERT INTO orders (user_id, copy_id, status, dueDate, lendingType, createdAt) VALUES (?,?,?,?,?,?)";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM orders WHERE id=?";
    private static final String SELECT_ALL_SQL = "SELECT * FROM orders";
    private static final String SELECT_BY_STATUS_SQL = "SELECT * FROM orders WHERE status=?";
    private static final String SELECT_BY_STATUS_PAGED_SQL = "SELECT * FROM orders WHERE status=? LIMIT ? OFFSET ?";
    private static final String COUNT_BY_STATUS_SQL = "SELECT COUNT(*) FROM orders WHERE status=?";
    private static final String UPDATE_SQL = "UPDATE orders SET user_id=?, copy_id=?, status=?, dueDate=?, lendingType=? WHERE id=?";
    private static final String DELETE_SQL = "DELETE FROM orders WHERE id=?";

    @Override
    public Long save(Order order) throws SQLException {
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, order.getUser().getId());
            ps.setLong(2, order.getCopy().getId());
            ps.setString(3, order.getStatus().name());
            if (order.getDueDate() != null) {
                ps.setDate(4, Date.valueOf(order.getDueDate()));
            } else {
                ps.setNull(4, Types.DATE);
            }
            ps.setString(5, order.getLendingType() != null ? order.getLendingType().name() : null);
            ps.setTimestamp(6, Timestamp.valueOf(order.getCreatedAt()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    order.setId(id);
                    return id;
                }
            }
        }
        return null;
    }

    @Override
    public Order findById(Long id) throws SQLException {
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
    public List<Order> findAll() throws SQLException {
        List<Order> list = new ArrayList<>();
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
    public List<Order> findByStatus(OrderStatus status) throws SQLException {
        List<Order> list = new ArrayList<>();
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_STATUS_SQL)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Order> findByStatusPaged(OrderStatus status, int offset, int limit) throws SQLException {
        List<Order> list = new ArrayList<>();
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_STATUS_PAGED_SQL)) {
            ps.setString(1, status.name());
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public long countByStatus(OrderStatus status) throws SQLException {
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(COUNT_BY_STATUS_SQL)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return 0;
    }

    @Override
    public void update(Order order) throws SQLException {
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            ps.setLong(1, order.getUser().getId());
            ps.setLong(2, order.getCopy().getId());
            ps.setString(3, order.getStatus().name());
            if (order.getDueDate() != null) {
                ps.setDate(4, Date.valueOf(order.getDueDate()));
            } else {
                ps.setNull(4, Types.DATE);
            }
            ps.setString(5, order.getLendingType() != null ? order.getLendingType().name() : null);
            ps.setLong(6, order.getId());
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

    private Order mapRow(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getLong("id"));
        o.setStatus(OrderStatus.valueOf(rs.getString("status")));
        Date due = rs.getDate("dueDate");
        if (due != null) {
            o.setDueDate(due.toLocalDate());
        }
        String lt = rs.getString("lendingType");
        if (lt != null) {
            o.setLendingType(LendingType.valueOf(lt));
        }
        Timestamp ts = rs.getTimestamp("createdAt");
        if (ts != null) {
            o.setCreatedAt(ts.toLocalDateTime());
        } else {
            o.setCreatedAt(LocalDateTime.now());
        }
        // populate minimal user and copy references for higher-level logic
        long userId = rs.getLong("user_id");
        if (userId != 0) {
            edu.epam.fop.model.User u = new edu.epam.fop.model.User();
            u.setId(userId);
            o.setUser(u);
        }
        long copyId = rs.getLong("copy_id");
        if (copyId != 0) {
            edu.epam.fop.model.BookCopy bc = new edu.epam.fop.model.BookCopy();
            bc.setId(copyId);
            o.setCopy(bc);
        }
        return o;
    }
} 