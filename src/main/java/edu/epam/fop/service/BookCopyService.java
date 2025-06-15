package edu.epam.fop.service;

import edu.epam.fop.model.BookCopy;
import edu.epam.fop.dao.BookCopyDao;
import edu.epam.fop.dao.OrderDao;
import edu.epam.fop.model.Order;
import edu.epam.fop.model.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookCopyService {

    private final BookCopyDao copyDao;
    private final OrderDao orderDao;

    private static final Logger log = LoggerFactory.getLogger(BookCopyService.class);

    @Autowired
    public BookCopyService(BookCopyDao copyDao, OrderDao orderDao) {
        this.copyDao = copyDao;
        this.orderDao = orderDao;
    }

    public List<BookCopy> findByBookId(Long bookId) {
        try {
            List<BookCopy> list = copyDao.findByBookId(bookId);
            // enrich with orders for copies that might be issued
            for (BookCopy c : list) {
                if (c.getStatus() == edu.epam.fop.model.Status.ISSUED) {
                    try {
                        List<Order> orders = orderDao.findByStatus(OrderStatus.ISSUED).stream()
                                .filter(o -> o.getCopy() != null && o.getCopy().getId().equals(c.getId()))
                                .toList();
                        c.getOrders().addAll(orders);
                    } catch(Exception ex){ log.warn("Failed to load orders for copy {}", c.getId(), ex);}
                }
            }
            return list;
        } catch(Exception e){ throw new RuntimeException(e);}
    }

    @Transactional
    public BookCopy save(BookCopy copy) {
        try {
            copyDao.save(copy);
            return copy;
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public List<BookCopy> findAll() {
        try {
            return copyDao.findAll();
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public boolean existsByInventoryNumber(String inventoryNumber) {
        try {
            return copyDao.findAll().stream()
                    .anyMatch(c -> inventoryNumber.equals(c.getInventoryNumber()));
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }
} 