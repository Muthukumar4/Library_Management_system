package com.example.lsmbackend.controler;

import com.example.lsmbackend.model.Dashboard;
import com.example.lsmbackend.service.DashboardServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class Dashcontroler {
    @Autowired
    private DashboardServices dashboardService;

    @GetMapping
    public Dashboard dashboard() {
        return dashboardService.getDashboardData();
    }
}
