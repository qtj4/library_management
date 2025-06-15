package edu.epam.fop.controller;

import edu.epam.fop.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/reports")
public class AdminReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping
    public String dashboard(Model model){
        model.addAttribute("issued", reportService.totalIssued());
        model.addAttribute("top", reportService.mostRequested());
        return "admin/report";
    }
} 