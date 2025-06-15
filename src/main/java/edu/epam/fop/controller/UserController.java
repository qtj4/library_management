package edu.epam.fop.controller;

import edu.epam.fop.dto.UserForm;
import edu.epam.fop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userForm", new UserForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("userForm") UserForm form, RedirectAttributes redirectAttributes) {
        try {
            userService.registerUser(form.getUsername(), form.getPassword());
            redirectAttributes.addFlashAttribute("success", "Registration successful!");
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
} 