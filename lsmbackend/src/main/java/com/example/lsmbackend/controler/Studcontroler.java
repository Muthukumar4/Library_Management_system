package com.example.lsmbackend.controler;

import com.example.lsmbackend.dto.LoginRequest;
import com.example.lsmbackend.dto.StudentResponseDto;
import com.example.lsmbackend.model.Student;
import com.example.lsmbackend.service.Studeservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/students")
@CrossOrigin("*")
public class   Studcontroler {

    @Autowired
    private Studeservice stsrv;

    @PostMapping("/add")
    public StudentResponseDto addStudent(@RequestBody Student stud){
        return StudentResponseDto.from(stsrv.addStudent(stud));
    }

    @GetMapping("/getall")
    public List<StudentResponseDto> getAllStudent(){
        return stsrv.getAllStudent().stream()
                .map(StudentResponseDto::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/department/{department}")
    public List<StudentResponseDto> getStudentsByDepartment(@PathVariable String department) {
        return stsrv.getStudentsByDepartment(department).stream()
                .map(StudentResponseDto::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/rollNumber/{rollNumber}")
    public StudentResponseDto getStudentbyRoll(@PathVariable("rollNumber") String rollNumber, Authentication authentication){
        ensureStudentOwnerOrPrivileged(authentication, rollNumber);
        return StudentResponseDto.from(stsrv.getStudentbyroll(rollNumber));
    }

    @GetMapping("/barcode/{barcode}")
    public StudentResponseDto getStudentbyBarcode(@PathVariable String barcode){
        return StudentResponseDto.from(stsrv.getStudentbyBarcode(barcode));
    }

    @PutMapping("/update/{rollNumber}")
    public StudentResponseDto updateStudent(
            @PathVariable("rollNumber") String rollnumber,
            @RequestBody Student stud){

        return StudentResponseDto.from(stsrv.updateStudent(rollnumber, stud));
    }

    @PostMapping("/student/login")
    public ResponseEntity<?> loginStudent(@RequestBody LoginRequest request) {

        String result = stsrv.studentLogin(
                request.getRollNumber(),
                request.getPassword()
        );

        return ResponseEntity.ok(result);
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
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only access your own student record");
        }
    }

}
