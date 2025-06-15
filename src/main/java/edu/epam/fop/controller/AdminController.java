package edu.epam.fop.controller;

import edu.epam.fop.model.Book;
import edu.epam.fop.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
public class AdminController {

    private final BookService bookService;

    @Autowired
    public AdminController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/admin/books")
    public String listBooks(Model model) {
        model.addAttribute("books", bookService.findAll());
        return "admin/book-list";
    }

    @GetMapping("/admin/books/new")
    public String newBookForm(Model model) {
        model.addAttribute("book", new Book());
        return "admin/book-form";
    }

    @PostMapping("/admin/books")
    public String saveBook(@ModelAttribute("book") @Valid Book book, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/book-form";
        }
        bookService.save(book);
        redirectAttributes.addFlashAttribute("success", "Book added!");
        return "redirect:/admin/books";
    }
} 