package com.example.lsmbackend.dto;

public class LoginResponseDto {
    private boolean success;
    private String message;
    private String role;
    private String name;
    private String staffCode;
    private String email;

    public LoginResponseDto() {
    }

    public LoginResponseDto(boolean success, String message, String role,
                            String name, String staffCode, String email) {
        this.success = success;
        this.message = message;
        this.role = role;
        this.name = name;
        this.staffCode = staffCode;
        this.email = email;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public String getRole() {
        return role;
    }

    public String getName() {
        return name;
    }

    public String getStaffCode() {
        return staffCode;
    }

    public String getEmail() {
        return email;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStaffCode(String staffCode) {
        this.staffCode = staffCode;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
