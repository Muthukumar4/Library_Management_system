package com.example.lsmbackend.service;

import com.example.lsmbackend.model.Dashboard;
import com.example.lsmbackend.repository.Bookrepo;
import com.example.lsmbackend.repository.Issuerepo;
import com.example.lsmbackend.repository.Staffrepo;
import com.example.lsmbackend.repository.Studerepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DashboardServices {

    @Autowired
    private Bookrepo bookRepo;

    @Autowired
    private Issuerepo issuerepo;

    @Autowired
    private Studerepo studentRepo;

    @Autowired
    private Staffrepo staffrepo;

    public Dashboard getDashboardData() {

        Dashboard dto = new Dashboard();

        dto.setTotalBooks(bookRepo.count());
        dto.setAvailableBooks(
                bookRepo.totalAvailableBooks() == null ? 0 :
                        bookRepo.totalAvailableBooks()
        );
        dto.setIssuedBooks(issuerepo.countByStatus("ISSUED"));
        dto.setTotalStudents(studentRepo.count());
        dto.setTotalStaff(staffrepo.count());


        return dto;
    }
}
