package com.mycompany.assignment;

public class Administrator extends User {
    public Administrator(String userID, String name, String contactNum, String passwordHash) {
        super(userID, name, contactNum, passwordHash);
    }

    @Override
    public String getRole() {
        return Roles.ADMINISTRATOR;
    }
}
