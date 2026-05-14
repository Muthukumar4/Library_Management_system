package com.example.lsmbackend.dto;

import com.example.lsmbackend.model.Staff;
import com.example.lsmbackend.model.StaffType;

public class StaffResponseDto {
    private Long staffId;
    private String staffCode;
    private String name;
    private String department;
    private String email;
    private String phone;
    private StaffType staffType;
    private String barcode;
    private Boolean active;

    public static StaffResponseDto from(Staff staff) {
        if (staff == null) {
            return null;
        }

        StaffResponseDto dto = new StaffResponseDto();
        dto.setStaffId(staff.getStaffId());
        dto.setStaffCode(staff.getStaffCode());
        dto.setName(staff.getName());
        dto.setDepartment(staff.getDepartment());
        dto.setEmail(staff.getEmail());
        dto.setPhone(staff.getPhone());
        dto.setStaffType(staff.getStaffType());
        dto.setBarcode(staff.getBarcode());
        dto.setActive(staff.getActive());
        return dto;
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public String getStaffCode() {
        return staffCode;
    }

    public void setStaffCode(String staffCode) {
        this.staffCode = staffCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public StaffType getStaffType() {
        return staffType;
    }

    public void setStaffType(StaffType staffType) {
        this.staffType = staffType;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
