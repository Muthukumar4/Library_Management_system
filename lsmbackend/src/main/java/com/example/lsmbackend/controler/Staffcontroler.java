package com.example.lsmbackend.controler;

import com.example.lsmbackend.dto.StaffResponseDto;
import com.example.lsmbackend.model.Staff;
import com.example.lsmbackend.model.StaffType;
import com.example.lsmbackend.service.Staffservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/staff")
@CrossOrigin("*")
public class Staffcontroler {

    @Autowired
    private Staffservice staffsrv;

    @PostMapping("/add")
    public StaffResponseDto addStaff(@RequestBody Staff staff){
        return StaffResponseDto.from(staffsrv.addStaff(staff));
    }

    @GetMapping("/all")
    public List<StaffResponseDto> getAllStaff(){
        return staffsrv.getAllStaff().stream()
                .map(StaffResponseDto::from)
                .collect(Collectors.toList());
    }
    @GetMapping("/code/{staffCode}")
    public StaffResponseDto getStaffbycode(@PathVariable String staffCode, Authentication authentication){
        ensureStaffOwnerOrAdmin(authentication, staffCode);
        return StaffResponseDto.from(staffsrv.getStaffbycode(staffCode));
    }


    @GetMapping("/barcode/{barcode}")
    public StaffResponseDto getStaffbyBarcode(@PathVariable String barcode){
        return StaffResponseDto.from(staffsrv.getStaffbyBarcode(barcode));
    }

    @GetMapping("/staffType/{type}")
    public List<StaffResponseDto> getByType(@PathVariable StaffType type) {
        return staffsrv.findByStaffType(type).stream()
                .map(StaffResponseDto::from)
                .collect(Collectors.toList());
    }
    @PutMapping("/update/{staffCode}")
    public StaffResponseDto updateStaff(
            @PathVariable String staffCode,
            @RequestBody Staff staff){

        return StaffResponseDto.from(staffsrv.updateStaff(staffCode, staff));
    }

    private void ensureStaffOwnerOrAdmin(Authentication authentication, String staffCode) {
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        boolean isStaff = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_STAFF".equals(authority.getAuthority()));

        if (isAdmin || !isStaff) {
            return;
        }

        if (!authentication.getName().equalsIgnoreCase(staffCode)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only access your own staff record");
        }
    }
}
