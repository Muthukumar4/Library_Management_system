package com.example.lsmbackend.dto;

import java.util.List;

public class SessionValidationResponse {
    private boolean authenticated;
    private String username;
    private String role;
    private List<String> roles;

    public SessionValidationResponse() {
    }

    public SessionValidationResponse(boolean authenticated, String username, String role, List<String> roles) {
        this.authenticated = authenticated;
        this.username = username;
        this.role = role;
        this.roles = roles;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
