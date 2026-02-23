package com.reservation.model;

public class User {
    private String id;
    private String username;
    private String password;
    private Role role;

    public enum Role {
        ADMIN, GESTIONNAIRE, CLIENT
    }

    public User(String id, String username, String password, Role role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }

    public void setPassword(String password) { this.password = password; }
    public void setUsername(String username) { this.username = username; }
}
