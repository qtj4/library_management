package edu.epam.fop.dao;

import edu.epam.fop.model.Order;
import edu.epam.fop.model.OrderStatus;

import java.sql.SQLException;
import java.util.List;

public interface OrderDao {

    Long save(Order order) throws SQLException;

    Order findById(Long id) throws SQLException;

    List<Order> findAll() throws SQLException;

    List<Order> findByStatus(OrderStatus status) throws SQLException;

    List<Order> findByStatusPaged(OrderStatus status, int offset, int limit) throws SQLException;

    long countByStatus(OrderStatus status) throws SQLException;

    void update(Order order) throws SQLException;

    void deleteById(Long id) throws SQLException;
} 