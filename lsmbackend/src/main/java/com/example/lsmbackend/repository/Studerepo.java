package com.example.lsmbackend.repository;

import com.example.lsmbackend.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Studerepo extends JpaRepository<Student,Long> {
    Optional<Student> findByBarcode(String barcode);
    Optional<Student> findByRollNumber(String rollNumber);
}
