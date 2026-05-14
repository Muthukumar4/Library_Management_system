package com.example.lsmbackend.dto;

import com.example.lsmbackend.model.Student;

public class StudentResponseDto {
    private Long studentId;
    private String rollNumber;
    private String name;
    private String department;
    private String email;
    private String year;
    private String phone;
    private String barcode;
    private Boolean active;

    public static StudentResponseDto from(Student student) {
        if (student == null) {
            return null;
        }

        StudentResponseDto dto = new StudentResponseDto();
        dto.setStudentId(student.getStudentId());
        dto.setRollNumber(student.getRollNumber());
        dto.setName(student.getName());
        dto.setDepartment(student.getDepartment());
        dto.setEmail(student.getEmail());
        dto.setYear(student.getYear());
        dto.setPhone(student.getPhone());
        dto.setBarcode(student.getBarcode());
        dto.setActive(student.getActive());
        return dto;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
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

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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
