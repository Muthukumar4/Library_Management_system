package com.example.lsmbackend.repository;

import com.example.lsmbackend.model.Staff;
import com.example.lsmbackend.model.StaffType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Staffrepo extends JpaRepository<Staff, Long> {
    Optional<Staff> findByStaffCode(String staffCode);

    Optional<Staff> findByBarcode(String barcode);

    Optional<Staff> findByEmail(String email);

    Optional<Staff> findByStaffCodeOrEmail(String staffCode, String email);

    List<Staff> findByStaffType(StaffType staffType);
}
