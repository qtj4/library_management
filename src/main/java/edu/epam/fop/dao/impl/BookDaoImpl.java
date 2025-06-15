package edu.epam.fop.dao.impl;

import edu.epam.fop.dao.BookDao;
import edu.epam.fop.dao.ConnectionPool;
import edu.epam.fop.model.Book;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class BookDaoImpl implements BookDao {

    private static final String INSERT_SQL = "INSERT INTO books (title, author, description) VALUES (?, ?, ?)";
    private static final String SELECT_BY_ID_SQL = "SELECT id, title, author, description FROM books WHERE id = ?";
    private static final String SELECT_ALL_SQL = "SELECT id, title, author, description FROM books";
    private static final String SELECT_PAGED_FILTER_SQL = "SELECT id, title, author, description FROM books WHERE (? IS NULL OR LOWER(title) LIKE ?) AND (? IS NULL OR LOWER(author) LIKE ?) LIMIT ? OFFSET ?";
    private static final String UPDATE_SQL = "UPDATE books SET title=?, author=?, description=? WHERE id=?";
    private static final String DELETE_SQL = "DELETE FROM books WHERE id=?";

    @Override
    public Long save(Book book) throws SQLException {
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getDescription());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    book.setId(id);
                    return id;
                }
            }
        }
        return null;
    }

    @Override
    public Book findById(Long id) throws SQLException {
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
    public List<Book> findAll() throws SQLException {
        List<Book> list = new ArrayList<>();
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
    public List<Book> findPaged(int offset, int limit, String titleFilter, String authorFilter) throws SQLException {
        List<Book> list = new ArrayList<>();
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_PAGED_FILTER_SQL)) {
            String tf = titleFilter!=null?"%"+titleFilter.toLowerCase()+"%":null;
            String af = authorFilter!=null?"%"+authorFilter.toLowerCase()+"%":null;
            ps.setString(1, tf);
            ps.setString(2, tf);
            ps.setString(3, af);
            ps.setString(4, af);
            ps.setInt(5, limit);
            ps.setInt(6, offset);
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public void update(Book book) throws SQLException {
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getDescription());
            ps.setLong(4, book.getId());
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

    private Book mapRow(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setId(rs.getLong("id"));
        b.setTitle(rs.getString("title"));
        b.setAuthor(rs.getString("author"));
        b.setDescription(rs.getString("description"));
        return b;
    }
} 