package edu.epam.fop.service;

import edu.epam.fop.model.*;
import edu.epam.fop.dao.BookCopyDao;
import edu.epam.fop.dao.BookDao;
import edu.epam.fop.dao.OrderDao;
import edu.epam.fop.dao.JdbcTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class OrderService {

    private final OrderDao orderDao;
    private final BookCopyDao copyDao;
    private final BookDao bookDao;

    @Autowired
    public OrderService(OrderDao orderDao, BookCopyDao copyDao, BookDao bookDao) {
        this.orderDao = orderDao;
        this.copyDao = copyDao;
        this.bookDao = bookDao;
    }

    /**
     * Creates a new {@link Order} for the given user and copy.
     * Reserves the copy and persists order inside a manual JDBC transaction.
     */
    public Order createOrder(User user, BookCopy copy, LendingType type) {
        copy.setStatus(Status.RESERVED);
        try {
            JdbcTransactionManager.begin();
            copyDao.update(copy);
            Order order = new Order(user, copy, OrderStatus.PENDING);
            order.setLendingType(type);
            orderDao.save(order);
            JdbcTransactionManager.commit();
            return order;
        } catch(Exception e){
            JdbcTransactionManager.rollback();
            throw new RuntimeException(e);
        }
    }

    /**
     * Lists orders belonging to the specified user.
     */
    public List<Order> findByUser(User user) {
        try {
            List<Order> list = orderDao.findAll().stream().filter(o->o.getUser()!=null && o.getUser().getId().equals(user.getId())).toList();
            list.forEach(this::enrich);
            return list;
        } catch(Exception e){ throw new RuntimeException(e);}
    }

    /**
     * Returns orders with the given status.
     */
    public List<Order> findByStatus(OrderStatus status) {
        try {
            List<Order> list = orderDao.findByStatus(status);
            list.forEach(this::enrich);
            return list;
        } catch(Exception e){ throw new RuntimeException(e);}
    }

    public List<Order> findByStatusPaged(OrderStatus status,int page,int size){
        try {
            List<Order> list = orderDao.findByStatusPaged(status,page*size,size);
            list.forEach(this::enrich);
            return list;
        } catch(Exception e){ throw new RuntimeException(e);}
    }

    public List<Order> findByUserPaged(User user,int page,int size){
        try {
            List<Order> list = orderDao.findAll().stream()
                    .filter(o->o.getUser()!=null && o.getUser().getId().equals(user.getId()))
                    .skip(page*size)
                    .limit(size)
                    .toList();
            list.forEach(this::enrich);
            return list;
        }catch(Exception e){ throw new RuntimeException(e);}
    }

    /**
     * Issues (hands out) a previously pending order – marks copy ISSUED and order ISSUED.
     */
    public void issueOrder(Long orderId) {
        try {
            JdbcTransactionManager.begin();
            Order order = orderDao.findById(orderId);
            enrich(order);
            if (order!=null){
                BookCopy copy = order.getCopy();
                copy.setStatus(Status.ISSUED);
                order.setStatus(OrderStatus.ISSUED);
                copyDao.update(copy);
                orderDao.update(order);
                JdbcTransactionManager.commit();
            }
        } catch(Exception e){
            JdbcTransactionManager.rollback();
            throw new RuntimeException(e);
        }
    }

    /**
     * Marks copy AVAILABLE again and order RETURNED.
     */
    public void returnOrder(Long orderId) {
        try {
            JdbcTransactionManager.begin();
            Order order = orderDao.findById(orderId);
            enrich(order);
            if (order!=null){
                BookCopy copy = order.getCopy();
                copy.setStatus(Status.AVAILABLE);
                order.setStatus(OrderStatus.RETURNED);
                copyDao.update(copy);
                orderDao.update(order);
                JdbcTransactionManager.commit();
            }
        } catch(Exception e){
            JdbcTransactionManager.rollback();
            throw new RuntimeException(e);
        }
    }

    public void issueOrderDetailed(Long orderId, LocalDate dueDate, LendingType type) {
        try {
            JdbcTransactionManager.begin();
            Order order = orderDao.findById(orderId);
            enrich(order);
            if (order!=null){
                BookCopy copy = order.getCopy();
                copy.setStatus(Status.ISSUED);
                order.setStatus(OrderStatus.ISSUED);
                order.setDueDate(dueDate);
                order.setLendingType(type);
                copyDao.update(copy);
                orderDao.update(order);
                JdbcTransactionManager.commit();
            }
        } catch(Exception e){
            JdbcTransactionManager.rollback();
            throw new RuntimeException(e);
        }
    }

    public void cancelOrder(Long orderId, User user) {
        try {
            JdbcTransactionManager.begin();
            Order order = orderDao.findById(orderId);
            enrich(order);
            if (order!=null && order.getStatus()==OrderStatus.PENDING && order.getUser()!=null && order.getUser().getId().equals(user.getId())){
                order.setStatus(OrderStatus.CANCELLED);
                BookCopy copy = order.getCopy();
                copy.setStatus(Status.AVAILABLE);
                copyDao.update(copy);
                orderDao.update(order);
                JdbcTransactionManager.commit();
            }
        } catch(Exception e){
            JdbcTransactionManager.rollback();
            throw new RuntimeException(e);
        }
    }

    private void enrich(Order o) {
        if (o == null) {
            return;
        }
        try {
            if (o.getCopy() == null) {
                return;
            }

            // Load full BookCopy information from DB (including stub book id)
            BookCopy fullCopy = copyDao.findById(o.getCopy().getId());
            if (fullCopy == null) {
                return;
            }

            // Always attempt to load full Book details if we know the book id
            Long bookId = null;
            if (fullCopy.getBook() != null) {
                bookId = fullCopy.getBook().getId();
            }

            if (bookId != null) {
                var book = bookDao.findById(bookId);
                if (book != null) {
                    fullCopy.setBook(book);
                } else {
                    // Ensure non-null reference to avoid template errors
                    edu.epam.fop.model.Book stub = new edu.epam.fop.model.Book();
                    stub.setId(bookId);
                    stub.setTitle("N/A");
                    fullCopy.setBook(stub);
                }
            } else {
                // bookId unknown – still attach stub book to avoid nulls in template
                edu.epam.fop.model.Book stub = new edu.epam.fop.model.Book();
                stub.setTitle("N/A");
                fullCopy.setBook(stub);
            }

            // replace the lightweight copy in the order with the fully enriched one
            o.setCopy(fullCopy);
        } catch (Exception ignored) {
        }
    }
} 