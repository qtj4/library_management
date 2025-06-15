package edu.epam.fop.controller;

import edu.epam.fop.model.Order;
import edu.epam.fop.model.OrderStatus;
import edu.epam.fop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/librarian/orders")
public class LibrarianController {

    private final OrderService orderService;

    @Autowired
    public LibrarianController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String pendingOrders(@RequestParam(value="page",defaultValue="0") int page,
                                @RequestParam(value="size",defaultValue="10") int size,
                                Model model) {
        List<Order> pending = orderService.findByStatusPaged(OrderStatus.PENDING,page,size);
        model.addAttribute("orders", pending);
        model.addAttribute("page",page);
        model.addAttribute("size",size);
        return "librarian/order-list";
    }

    @GetMapping("/{orderId}/confirm")
    public String confirmForm(@PathVariable Long orderId, Model model) {
        Order order = orderService.findByStatus(OrderStatus.PENDING).stream()
                .filter(o -> o.getId().equals(orderId))
                .findFirst()
                .orElse(null);
        if (order == null) {
            return "redirect:/librarian/orders";
        }
        model.addAttribute("order", order);
        model.addAttribute("types", edu.epam.fop.model.LendingType.values());
        return "librarian/order-confirm";
    }

    @PostMapping("/{orderId}/confirm")
    public String confirm(@PathVariable Long orderId,
                          @RequestParam("dueDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate dueDate,
                          @RequestParam("type") edu.epam.fop.model.LendingType type,
                          RedirectAttributes redirectAttributes) {
        orderService.issueOrderDetailed(orderId, dueDate, type);
        redirectAttributes.addFlashAttribute("success", "Order confirmed");
        return "redirect:/librarian/orders";
    }

    @PostMapping("/{orderId}/return")
    public String processReturn(@PathVariable Long orderId, RedirectAttributes redirectAttributes) {
        orderService.returnOrder(orderId);
        redirectAttributes.addFlashAttribute("success", "Book returned");
        return "redirect:/librarian/orders";
    }
} 