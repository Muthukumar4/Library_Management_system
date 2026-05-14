package com.example.lsmbackend.service;

import com.example.lsmbackend.model.Student;
import com.example.lsmbackend.repository.Studerepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Studeservice {

    @Autowired
    private Studerepo strepo;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public Student addStudent(Student stud) {
        if(stud.getPassword()==null||stud.getPassword().isBlank()){
            throw new RuntimeException("Password is required");

        }
        stud.setPassword(passwordEncoder.encode(stud.getPassword()));

        // Set active to true if not provided
        if (stud.getActive() == null) {
            stud.setActive(true);
        }

        return strepo.save(stud);
    }

    public List<Student> getAllStudent() {
        return strepo.findAll();
    }

    public List<Student> getStudentsByDepartment(String department) {
        if (department == null || department.trim().isEmpty()) {
            return List.of();
        }
        return strepo.findByDepartmentIgnoreCase(department.trim());
    }

    public Student getStudentbyroll(String rollno) {
        return strepo.findByRollNumber(rollno).orElse(null);
    }

    public Student getStudentbyBarcode(String barcode) {
        return strepo.findByBarcode(barcode).orElse(null);
    }

    public Student updateStudent(String rollnumber, Student stud) {
        Student st=strepo.findByRollNumber(rollnumber).orElse(null);

        if(st!=null){
            st.setName(stud.getName());
            st.setDepartment(stud.getDepartment());
            st.setEmail(stud.getEmail());
            st.setPhone(stud.getPhone());
            st.setActive(stud.getActive());

            return strepo.save(st);

        }
        return null;
    }
    public String studentLogin(String rollNumber, String password) {

        Student student = strepo.findByRollNumber(rollNumber)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (!Boolean.TRUE.equals(student.getActive())) {
            throw new RuntimeException("Student account is inactive");
        }

        if(passwordEncoder.matches(password, student.getPassword())) {
            return "Login Successful";
        } else {
            throw new RuntimeException("Invalid Password");
        }
    }
}
