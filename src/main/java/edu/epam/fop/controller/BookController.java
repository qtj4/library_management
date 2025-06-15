package edu.epam.fop.controller;

import edu.epam.fop.model.Book;
import edu.epam.fop.model.User;
import edu.epam.fop.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import edu.epam.fop.dao.UserDao;
import edu.epam.fop.service.OrderService;
import edu.epam.fop.model.Status;
import edu.epam.fop.model.BookCopy;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import edu.epam.fop.model.LendingType;

import java.util.List;
import java.util.Optional;

@Controller
public class BookController {

    private final BookService bookService;
    private final OrderService orderService;
    private final UserDao userDao;

    @Autowired
    public BookController(BookService bookService, OrderService orderService, UserDao userDao) {
        this.bookService = bookService;
        this.orderService = orderService;
        this.userDao = userDao;
    }

    // Catalogue with optional search by title
    @GetMapping("/books")
    public String listBooks(@RequestParam(value = "title", required = false) String title,
                            @RequestParam(value = "author", required = false) String author,
                            @RequestParam(value="page", defaultValue="0") int page,
                            @RequestParam(value="size", defaultValue="10") int size,
                            Model model) {
        List<Book> books = bookService.searchPaged(title, author, page, size);
        model.addAttribute("books", books);
        model.addAttribute("searchTitle", title);
        model.addAttribute("searchAuthor", author);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "book/book-list";
    }

    // Details of a single book
    @GetMapping("/books/{id}")
    public String bookDetails(@PathVariable("id") Long id, Model model) {
        Book book = bookService.findAll().stream()
                .filter(b -> b.getId().equals(id))
                .findFirst()
                .orElse(null);
        if (book == null) {
            return "redirect:/books";
        }
        boolean hasAvailable = book.getCopies().stream().anyMatch(c -> c.getStatus() == Status.AVAILABLE);
        model.addAttribute("book", book);
        model.addAttribute("canRequest", hasAvailable);
        return "book/book-details";
    }

    @PostMapping("/books/{id}/request")
    public String requestBook(@PathVariable("id") Long bookId,
                              @RequestParam("type") LendingType type,
                              RedirectAttributes redirectAttributes) {
        // find book
        Book book = bookService.findAll().stream()
                .filter(b -> b.getId().equals(bookId))
                .findFirst()
                .orElse(null);
        if (book == null) {
            redirectAttributes.addFlashAttribute("error", "Book not found");
            return "redirect:/books";
        }

        Optional<BookCopy> availableCopy = book.getCopies().stream()
                .filter(c -> c.getStatus() == Status.AVAILABLE)
                .findFirst();
        if (availableCopy.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No available copies");
            return "redirect:/books/" + bookId;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user;
        try { user = userDao.findByUsername(username);} catch(Exception e){ throw new RuntimeException(e);}
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "User not found");
            return "redirect:/books/" + bookId;
        }

        orderService.createOrder(user, availableCopy.get(), type);
        redirectAttributes.addFlashAttribute("success", "Request created");
        return "redirect:/orders/history";
    }
} 