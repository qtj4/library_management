package edu.epam.fop.dao;

import edu.epam.fop.model.Book;

import java.sql.SQLException;
import java.util.List;

public interface BookDao {

    Long save(Book book) throws SQLException;

    Book findById(Long id) throws SQLException;

    List<Book> findAll() throws SQLException;

    List<Book> findPaged(int offset, int limit, String titleFilter, String authorFilter) throws SQLException;

    void update(Book book) throws SQLException;

    void deleteById(Long id) throws SQLException;
} 