package com.mycompany.assignment;

public class AttendanceOfficer extends User {
    public AttendanceOfficer(String userID, String name, String contactNum, String passwordHash) {
        super(userID, name, contactNum, passwordHash);
    }

    @Override
    public String getRole() {
        return Roles.ATTENDANCE_OFFICER;
    }
}
