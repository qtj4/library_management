package edu.epam.fop.dao.impl;

import edu.epam.fop.dao.BookCopyDao;
import edu.epam.fop.dao.ConnectionPool;
import edu.epam.fop.model.BookCopy;
import edu.epam.fop.model.Status;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class BookCopyDaoImpl implements BookCopyDao {

    private static final String INSERT_SQL = "INSERT INTO book_copies (inventory_number, status, book_id) VALUES (?, ?, ?)";
    private static final String SELECT_BY_ID_SQL = "SELECT id, inventory_number, status, book_id FROM book_copies WHERE id=?";
    private static final String SELECT_ALL_SQL = "SELECT id, inventory_number, status, book_id FROM book_copies";
    private static final String SELECT_BY_BOOK_SQL = "SELECT id, inventory_number, status, book_id FROM book_copies WHERE book_id=?";
    private static final String UPDATE_SQL = "UPDATE book_copies SET inventory_number=?, status=?, book_id=? WHERE id=?";
    private static final String DELETE_SQL = "DELETE FROM book_copies WHERE id=?";

    @Override
    public Long save(BookCopy copy) throws SQLException {
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, copy.getInventoryNumber());
            ps.setString(2, copy.getStatus().name());
            if (copy.getBook() != null) {
                ps.setLong(3, copy.getBook().getId());
            } else {
                ps.setNull(3, Types.BIGINT);
            }
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
    public BookCopy findById(Long id) throws SQLException {
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
    public List<BookCopy> findAll() throws SQLException {
        List<BookCopy> list = new ArrayList<>();
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
    public List<BookCopy> findByBookId(Long bookId) throws SQLException {
        List<BookCopy> list = new ArrayList<>();
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_BOOK_SQL)) {
            ps.setLong(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public void update(BookCopy copy) throws SQLException {
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            ps.setString(1, copy.getInventoryNumber());
            ps.setString(2, copy.getStatus().name());
            if (copy.getBook() != null) {
                ps.setLong(3, copy.getBook().getId());
            } else {
                ps.setNull(3, Types.BIGINT);
            }
            ps.setLong(4, copy.getId());
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

    private BookCopy mapRow(ResultSet rs) throws SQLException {
        BookCopy bc = new BookCopy();
        bc.setId(rs.getLong("id"));
        bc.setInventoryNumber(rs.getString("inventory_number"));
        bc.setStatus(Status.valueOf(rs.getString("status")));
        long bid = rs.getLong("book_id");
        if(bid!=0){
            edu.epam.fop.model.Book b = new edu.epam.fop.model.Book();
            b.setId(bid);
            bc.setBook(b);
        }
        return bc;
    }
} 