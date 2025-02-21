package com.ObjectClass;

public class User {
    private String name;
    private int id;
    private String password;
    private String access;

    // Constructor with parameters
    public User( int id, String name, String password, String access) {
        this.name = name;
        this.id = id;
        this.password = password;
        this.access = access;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAccess() { return access; }
    public void setAccess(String access) { this.access = access; }

    public void printUserInfo() {
        System.out.println("Name: " + name);
        System.out.println("Id: " + id);
        System.out.println("Password: " + password);
        System.out.println("Access: " + access);
    }
}
