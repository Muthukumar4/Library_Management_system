package com.example.lsmbackend.controler;

import com.example.lsmbackend.model.Staff;
import com.example.lsmbackend.model.StaffType;
import com.example.lsmbackend.model.Student;
import com.example.lsmbackend.service.Staffservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@CrossOrigin("*")
public class Staffcontroler {

    @Autowired
    private Staffservice staffsrv;

    @PostMapping("/add")
    public Staff addStaff(@RequestBody Staff staff){
        return staffsrv.addStaff(staff);
    }

    @GetMapping("/all")
    public List<Staff> getAllStaff(){
        return staffsrv.getAllStaff();
    }
    @GetMapping("/code/{staffCode}")
    public Staff getStaffbycode(@PathVariable String staffCode){
        return staffsrv.getStaffbycode(staffCode);
    }

    @GetMapping("/barcode/{barcode}")
    public Staff getStaffbyBarcode(@PathVariable String barcode){
        return staffsrv.getStaffbyBarcode(barcode);
    }

    @GetMapping("/staffType/{type}")
    public List<Staff> getByType(@PathVariable StaffType type) {
        return staffsrv.findByStaffType(type);
    }
    @PutMapping("/update/{staffCode}")
    public Staff updateStaff(
            @PathVariable String staffCode,
            @RequestBody Staff staff){

        return staffsrv.updateStaff(staffCode, staff);
    }
}
