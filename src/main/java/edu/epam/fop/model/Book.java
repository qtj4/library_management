package edu.epam.fop.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String author;

    @Column(length = 1000)
    private String description;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<BookCopy> copies = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "book_user",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> users;

    public Book() {
    }

    public Book(String title, String author, String description) {
        this.title = title;
        this.author = author;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<BookCopy> getCopies() {
        return copies;
    }

    public void setCopies(List<BookCopy> copies) {
        this.copies = copies;
    }

    public void addCopy(BookCopy copy) {
        copy.setBook(this);
        this.copies.add(copy);
    }

    public Set<User> getUsers() {
        return users;
    }

    /* -------- Builder pattern -------- */
    public static Builder builder(){
        return new Builder();
    }

    public static class Builder{
        private final Book book = new Book();

        public Builder id(Long id){ book.setId(id); return this; }
        public Builder title(String title){ book.setTitle(title); return this; }
        public Builder author(String author){ book.setAuthor(author); return this; }
        public Builder description(String desc){ book.setDescription(desc); return this; }

        public Book build(){ return book; }
    }
} 