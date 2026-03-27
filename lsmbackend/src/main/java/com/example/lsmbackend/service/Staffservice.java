package com.example.lsmbackend.service;

import com.example.lsmbackend.model.Staff;
import com.example.lsmbackend.model.StaffType;
import com.example.lsmbackend.model.Student;
import com.example.lsmbackend.repository.Staffrepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Staffservice {

    @Autowired
    private Staffrepo staffrepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Staff addStaff(Staff staff) {

        if (staff.getPassword() == null || staff.getPassword().isEmpty()) {
            throw new RuntimeException("Password is required");
        }

        // 🔐 Encrypt password
        staff.setPassword(passwordEncoder.encode(staff.getPassword()));

        return staffrepo.save(staff);
    }

    public List<Staff> getAllStaff() {
        return staffrepo.findAll();
    }

    public Staff getStaffbycode(String staffCode) {
        return staffrepo.findByStaffCode(staffCode).orElse(null);
    }

    public Staff getStaffbyBarcode(String barcode) {
        return staffrepo.findByBarcode(barcode).orElse(null);
    }

    public Staff updateStaff(String staffCode, Staff staff) {
        Staff st=staffrepo.findByStaffCode(staffCode).orElseThrow(() ->new RuntimeException("Staff not Found"));
        st.setName(staff.getName());
        st.setDepartment(staff.getDepartment());
        st.setEmail(staff.getEmail());
        st.setPhone(staff.getPhone());

        if (!staff.isActive()) {
            st.setActive(staff.isActive());
        }
        return staffrepo.save(st);

    }

    public List<Staff> findByStaffType(StaffType type) {
        return staffrepo.findByStaffType(type);
    }
}
