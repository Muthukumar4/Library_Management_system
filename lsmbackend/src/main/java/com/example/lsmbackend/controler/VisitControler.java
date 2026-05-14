package com.example.lsmbackend.controler;

import com.example.lsmbackend.dto.VisitRecordDto;
import com.example.lsmbackend.dto.VisitScanRequest;
import com.example.lsmbackend.dto.VisitScanResponse;
import com.example.lsmbackend.service.VisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/visits")
@CrossOrigin("*")
public class VisitControler {

    @Autowired
    private VisitService visitService;

    @PostMapping("/scan")
    public VisitScanResponse scanVisit(@RequestBody VisitScanRequest request) {
        return visitService.scanVisit(request.getScannedId());
    }

    @GetMapping("/all")
    public List<VisitRecordDto> getAllVisits() {
        return visitService.getAllVisits();
    }

    @GetMapping("/student/{rollNumber}")
    public List<VisitRecordDto> getStudentVisits(@PathVariable String rollNumber, Authentication authentication) {
        ensureStudentOwnerOrPrivileged(authentication, rollNumber);
        return visitService.getStudentVisits(rollNumber);
    }

    @GetMapping("/staff-view")
    public List<VisitRecordDto> getStaffViewVisits() {
        return visitService.getStaffViewVisits();
    }

    @GetMapping("/inside-count")
    public Map<String, Long> getInsideCount() {
        return Map.of("count", visitService.getInsideCount());
    }

    private void ensureStudentOwnerOrPrivileged(Authentication authentication, String rollNumber) {
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        boolean isStudent = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_STUDENT".equals(authority.getAuthority()));
        boolean isPrivileged = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()) || "ROLE_STAFF".equals(authority.getAuthority()));

        if (!isStudent || isPrivileged) {
            return;
        }

        if (!authentication.getName().equalsIgnoreCase(rollNumber)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only access your own visit history");
        }
    }
}
