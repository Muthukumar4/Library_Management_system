package com.example.lsmbackend.service;

import com.example.lsmbackend.dto.LoginResponseDto;
import com.example.lsmbackend.dto.StaffLoginRequest;
import com.example.lsmbackend.model.Staff;
import com.example.lsmbackend.repository.Staffrepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private Staffrepo staffrepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public LoginResponseDto loginStaff(StaffLoginRequest request) {

        String loginId = request.getLoginId() == null ? "" : request.getLoginId().trim();
        String password = request.getPassword() == null ? "" : request.getPassword().trim();

        if (loginId.isEmpty()) {
            throw new RuntimeException("Staff code or email is required");
        }

        if (password.isEmpty()) {
            throw new RuntimeException("Password is required");
        }

        Staff staff = staffrepo.findByStaffCodeOrEmail(loginId, loginId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        boolean passwordMatched = passwordEncoder.matches(password, staff.getPassword());

        if (!passwordMatched) {
            throw new RuntimeException("Invalid password");
        }

        return new LoginResponseDto(
                true,
                "Login successful",
                "STAFF",
                staff.getName(),
                staff.getStaffCode(),
                staff.getEmail()
        );
    }
}

