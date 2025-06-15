package edu.epam.fop.controller;

import edu.epam.fop.model.BookCopy;
import edu.epam.fop.service.BookCopyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/librarian/copies")
public class LibrarianCopyController {

    private final BookCopyService copyService;

    @Autowired
    public LibrarianCopyController(BookCopyService copyService) {
        this.copyService = copyService;
    }

    @GetMapping
    public String listAll(Model model) {
        List<BookCopy> copies = copyService.findAll();
        model.addAttribute("copies", copies);
        return "librarian/copy-list";
    }
} 