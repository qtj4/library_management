package edu.epam.fop.service;

import edu.epam.fop.model.Book;
import edu.epam.fop.dao.BookDao;
import edu.epam.fop.dao.BookCopyDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookService {

    private final BookDao bookDao;
    private final BookCopyDao copyDao;

    private static final Logger log = LoggerFactory.getLogger(BookService.class);

    @Autowired
    public BookService(BookDao bookDao, BookCopyDao copyDao) {
        this.bookDao = bookDao;
        this.copyDao = copyDao;
    }

    public List<Book> findAll() {
        try {
            List<Book> list = bookDao.findAll();
            for (Book b: list) {
                try { b.setCopies(copyDao.findByBookId(b.getId()));} catch(Exception ex){ log.warn("Failed to load copies for book {}", b.getId(), ex);}
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Book> searchByTitle(String title) {
        if (title == null || title.isBlank()) {
            return findAll();
        }
        try {
            return findAll().stream()
                    .filter(b -> b.getTitle()!=null && b.getTitle().toLowerCase().contains(title.toLowerCase()))
                    .toList();
        } catch (Exception e) { throw new RuntimeException(e);}
    }

    public List<Book> search(String title, String author) {
        boolean hasTitle = title != null && !title.isBlank();
        boolean hasAuthor = author != null && !author.isBlank();
        try {
            return findAll().stream()
                    .filter(b -> (!hasTitle || (b.getTitle()!=null && b.getTitle().toLowerCase().contains(title.toLowerCase()))) &&
                                 (!hasAuthor || (b.getAuthor()!=null && b.getAuthor().toLowerCase().contains(author.toLowerCase()))))
                    .toList();
        } catch (Exception e) { throw new RuntimeException(e);}
    }

    @Transactional
    public Book save(Book book) {
        try {
            Long id = bookDao.save(book);
            if (id!=null) { book.setId(id);} else {}
            return book;
        } catch (Exception e) { throw new RuntimeException(e);}
    }

    public List<Book> searchPaged(String title, String author, int page, int size){
        try {
            int offset = page*size;
            List<Book> list = bookDao.findPaged(offset,size,title,author);
            for(Book b:list){
                try { b.setCopies(copyDao.findByBookId(b.getId()));}catch(Exception ex){ log.warn("Failed to load copies for book {}", b.getId(), ex);}
            }
            return list;
        }catch(Exception e){ throw new RuntimeException(e);}    }
} 