package edu.epam.fop.controller;

import edu.epam.fop.dao.RoleDao;
import edu.epam.fop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class UserAdminController {

    @Autowired private UserService userService;
    @Autowired private RoleDao roleDao;

    @GetMapping
    public String list(@RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "size", defaultValue = "10") int size,
                       Model model) {
        model.addAttribute("users", userService.findPaged(page, size));
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "admin/user-list";
    }

    @GetMapping("/new")
    public String form(Model model){
        try {
            model.addAttribute("roles", roleDao.findAll());
        } catch(Exception e){ throw new RuntimeException(e);}
        return "admin/user-form";
    }

    @PostMapping
    public String create(@RequestParam String username,
                         @RequestParam String password,
                         @RequestParam List<String> roles){
        userService.create(username,password,roles);
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/block")
    public String block(@PathVariable Long id){
        userService.toggleBlock(id);
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id){
        userService.delete(id);
        return "redirect:/admin/users";
    }
} 