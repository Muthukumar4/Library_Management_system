package com.example.lsmbackend.controler;

import com.example.lsmbackend.dto.AdminBookUpdateRequest;
import com.example.lsmbackend.dto.AdminDeleteRequest;
import com.example.lsmbackend.dto.AdminLoginRequest;
import com.example.lsmbackend.dto.AdminStaffUpdateRequest;
import com.example.lsmbackend.dto.AdminStudentUpdateRequest;
import com.example.lsmbackend.model.Book;
import com.example.lsmbackend.model.Staff;
import com.example.lsmbackend.model.Student;
import com.example.lsmbackend.service.AdminActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin("*")
public class AdminRecordController {

    @Autowired
    private AdminActionService adminActionService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AdminLoginRequest request) {
        boolean ok = adminActionService.loginAdmin(request.getUsername(), request.getPassword());
        if (!ok) {
            throw new RuntimeException("Invalid admin username or password");
        }
        return ResponseEntity.ok(Map.of("success", true, "role", "ADMIN"));
    }

    @PutMapping("/students/{rollNumber}")
    public Student updateStudent(@PathVariable String rollNumber, @RequestBody AdminStudentUpdateRequest request) {
        return adminActionService.updateStudent(rollNumber, request);
    }

    @DeleteMapping("/students/{rollNumber}")
    public ResponseEntity<?> deleteStudent(@PathVariable String rollNumber, @RequestBody AdminDeleteRequest request) {
        adminActionService.deleteStudent(rollNumber, request);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PutMapping("/staff/{staffCode}")
    public Staff updateStaff(@PathVariable String staffCode, @RequestBody AdminStaffUpdateRequest request) {
        return adminActionService.updateStaff(staffCode, request);
    }

    @DeleteMapping("/staff/{staffCode}")
    public ResponseEntity<?> deleteStaff(@PathVariable String staffCode, @RequestBody AdminDeleteRequest request) {
        adminActionService.deleteStaff(staffCode, request);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PutMapping("/books/{bookId}")
    public Book updateBook(@PathVariable Long bookId, @RequestBody AdminBookUpdateRequest request) {
        return adminActionService.updateBook(bookId, request);
    }

    @DeleteMapping("/books/{bookId}")
    public ResponseEntity<?> deleteBook(@PathVariable Long bookId, @RequestBody AdminDeleteRequest request) {
        adminActionService.deleteBook(bookId, request);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
