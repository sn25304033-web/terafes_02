package com.hotel.model;

public class User {
    private int id;
    private String fullName;
    private String email;
    private String passwordHash;
    private String phone;
    private String address;
    private String role;
    private boolean emailVerified; // CUSTOMER or ADMIN

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    private String lang = "en";
    public String getLang() { return lang; }
    public void setLang(String lang) { this.lang = lang; }

    public boolean isAdmin() { return "ADMIN".equalsIgnoreCase(role); }
}
