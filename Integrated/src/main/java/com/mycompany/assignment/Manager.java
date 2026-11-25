package com.mycompany.assignment;

public class Manager extends User {
    public Manager(String userID, String name, String contactNum, String passwordHash) {
        super(userID, name, contactNum, passwordHash);
    }

    @Override
    public String getRole() {
        return Roles.MANAGER;
    }
}
