package edu.epam.fop.dao;

import edu.epam.fop.model.BookCopy;

import java.sql.SQLException;
import java.util.List;

public interface BookCopyDao {

    Long save(BookCopy copy) throws SQLException;

    BookCopy findById(Long id) throws SQLException;

    List<BookCopy> findAll() throws SQLException;

    List<BookCopy> findByBookId(Long bookId) throws SQLException;

    void update(BookCopy copy) throws SQLException;

    void deleteById(Long id) throws SQLException;
} 