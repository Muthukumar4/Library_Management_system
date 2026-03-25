package com.example.lsmbackend.controler;

import com.example.lsmbackend.dto.LoginResponseDto;
import com.example.lsmbackend.dto.StaffLoginRequest;
import com.example.lsmbackend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/staff/login")
    public ResponseEntity<LoginResponseDto> loginStaff(@RequestBody StaffLoginRequest request) {
        LoginResponseDto response = authService.loginStaff(request);
        return ResponseEntity.ok(response);
    }
}