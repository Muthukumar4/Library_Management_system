package com.example.lsmbackend.controler;

import com.example.lsmbackend.dto.LoginRequest;
import com.example.lsmbackend.model.Student;
import com.example.lsmbackend.service.Studeservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@CrossOrigin("*")
public class   Studcontroler {

    @Autowired
    private Studeservice stsrv;

    @PostMapping("/add")
    public Student addStudent(@RequestBody Student stud){
        return stsrv.addStudent(stud);
    }

    @GetMapping("/getall")
    public List<Student> getAllStudent(){
        return stsrv.getAllStudent();
    }

    @GetMapping("/rollNumber/{rollNumber}")
    public Student getStudentbyRoll(@PathVariable String rollno){
        return stsrv.getStudentbyroll(rollno);
    }

    @GetMapping("/barcode/{barcode}")
    public Student getStudentbyBarcode(@PathVariable String barcode){
        return stsrv.getStudentbyBarcode(barcode);
    }

    @PutMapping("/update/{rollNumber}")
    public Student updateStudent(
            @PathVariable String rollnumber,
            @RequestBody Student stud){

        return stsrv.updateStudent(rollnumber, stud);
    }

    @PostMapping("/student/login")
    public ResponseEntity<?> loginStudent(@RequestBody LoginRequest request) {

        String result = stsrv.studentLogin(
                request.getRollNumber(),
                request.getPassword()
        );

        return ResponseEntity.ok(result);
    }


}
