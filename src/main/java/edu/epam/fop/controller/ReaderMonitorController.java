package edu.epam.fop.controller;

import edu.epam.fop.model.Order;
import edu.epam.fop.model.OrderStatus;
import edu.epam.fop.model.User;
import edu.epam.fop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/librarian/readers")
public class ReaderMonitorController {

    private final OrderService orderService;

    @Autowired
    public ReaderMonitorController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String list(Model model){
        List<Order> active = orderService.findByStatus(OrderStatus.PENDING);
        active.addAll(orderService.findByStatus(OrderStatus.ISSUED));
        Map<User,List<Order>> map = active.stream().collect(Collectors.groupingBy(Order::getUser));
        model.addAttribute("map", map);
        return "librarian/reader-list";
    }
} 