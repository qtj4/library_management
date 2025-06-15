package edu.epam.fop.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "book_copies")
public class BookCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inventory_number", unique = true, nullable = false)
    private String inventoryNumber;

    @Enumerated(EnumType.STRING)
    private Status status = Status.AVAILABLE;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id")
    private Book book;

    @OneToMany(mappedBy = "copy", fetch = FetchType.EAGER)
    private List<Order> orders = new ArrayList<>();

    public BookCopy() {
    }

    public BookCopy(String inventoryNumber, Status status) {
        this.inventoryNumber = inventoryNumber;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInventoryNumber() {
        return inventoryNumber;
    }

    public void setInventoryNumber(String inventoryNumber) {
        this.inventoryNumber = inventoryNumber;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public List<Order> getOrders() {
        return orders;
    }
} 