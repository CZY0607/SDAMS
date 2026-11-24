package com.mycompany.assignment;

public class FacilitiesOfficer extends User {
    public FacilitiesOfficer(String userID, String name, String contactNum, String passwordHash) {
        super(userID, name, contactNum, passwordHash);
    }

    @Override
    public String getRole() {
        return Roles.FACILITIES_OFFICER;
    }
}
