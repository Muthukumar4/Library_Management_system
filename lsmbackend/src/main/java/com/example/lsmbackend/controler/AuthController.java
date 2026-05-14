package com.example.lsmbackend.controler;

import com.example.lsmbackend.dto.LoginResponseDto;
import com.example.lsmbackend.dto.SessionValidationResponse;
import com.example.lsmbackend.dto.StaffLoginRequest;
import com.example.lsmbackend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/session")
    public ResponseEntity<SessionValidationResponse> validateSession(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String primaryRole = roles.stream()
                .filter(role -> role.startsWith("ROLE_"))
                .map(role -> role.substring(5))
                .findFirst()
                .orElse("");

        SessionValidationResponse response = new SessionValidationResponse(
                true,
                authentication.getName(),
                primaryRole,
                roles
        );
        return ResponseEntity.ok(response);
    }
}
