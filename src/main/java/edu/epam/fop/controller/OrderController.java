package edu.epam.fop.controller;

import edu.epam.fop.model.Order;
import edu.epam.fop.service.OrderService;
import edu.epam.fop.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class OrderController {

    private final OrderService orderService;
    private final UserDao userDao;

    @Autowired
    public OrderController(OrderService orderService, UserDao userDao) {
        this.orderService = orderService;
        this.userDao = userDao;
    }

    @GetMapping("/orders/history")
    public String myHistory(@RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(value = "size", defaultValue = "10") int size,
                            Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        edu.epam.fop.model.User user;
        try { user = userDao.findByUsername(username);} catch(Exception e){ throw new RuntimeException(e);}
        if (user == null) {
            return "redirect:/";
        }
        List<Order> orders = orderService.findByUserPaged(user, page, size);
        model.addAttribute("orders", orders);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "order/history";
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        edu.epam.fop.model.User user;
        try { user = userDao.findByUsername(username);} catch(Exception e){ throw new RuntimeException(e);}
        if (user != null) {
            orderService.cancelOrder(id, user);
        }
        return "redirect:/orders/history";
    }
} 