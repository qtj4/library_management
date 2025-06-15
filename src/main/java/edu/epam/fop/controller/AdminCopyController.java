package edu.epam.fop.controller;

import edu.epam.fop.model.Book;
import edu.epam.fop.model.BookCopy;
import edu.epam.fop.model.Status;
import edu.epam.fop.service.BookService;
import edu.epam.fop.service.BookCopyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/admin/books/{bookId}/copies")
public class AdminCopyController {

    private final BookService bookService;
    private final BookCopyService copyService;

    @Autowired
    public AdminCopyController(BookService bookService, BookCopyService copyService) {
        this.bookService = bookService;
        this.copyService = copyService;
    }

    @ModelAttribute("book")
    public Book loadBook(@PathVariable("bookId") Long bookId) {
        return bookService.findAll().stream()
                .filter(b -> b.getId().equals(bookId))
                .findFirst()
                .orElse(null);
    }

    @GetMapping
    public String listCopies(@ModelAttribute("book") Book book, Model model) {
        model.addAttribute("copies", copyService.findByBookId(book.getId()));
        return "admin/book-copy-list";
    }

    @GetMapping("/new")
    public String newCopyForm(@ModelAttribute("book") Book book, Model model) {
        BookCopy copy = new BookCopy();
        copy.setStatus(Status.AVAILABLE);
        model.addAttribute("copy", copy);
        return "admin/book-copy-form";
    }

    @PostMapping
    public String saveCopy(@ModelAttribute("book") Book book,
                            @ModelAttribute("copy") @Valid BookCopy copy,
                            BindingResult result,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/book-copy-form";
        }

        // Duplicate inventory number validation
        if (copyService.existsByInventoryNumber(copy.getInventoryNumber())) {
            result.rejectValue("inventoryNumber", "duplicate", "Inventory number already exists");
            return "admin/book-copy-form";
        }
        copy.setBook(book);
        copyService.save(copy);
        redirectAttributes.addFlashAttribute("success", "Copy added");
        return "redirect:/admin/books/" + book.getId() + "/copies";
    }
} 