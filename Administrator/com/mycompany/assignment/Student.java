package com.mycompany.assignment;

import java.util.Arrays;
import java.util.List;

public class Student extends User {
    public static final List<String> STATUSES = Arrays.asList("ACTIVE", "INACTIVE", "SUSPENDED");

    private String enrollmentStatus = "ACTIVE";

    public Student(String userID, String name, String contactNum, String passwordHash) {
        super(userID, name, contactNum, passwordHash);
    }

    public String getEnrollmentStatus() {
        return enrollmentStatus;
    }

    public void updateEnrollmentStatus(String status) {
        if (status == null || !STATUSES.contains(status)) {
            throw new IllegalArgumentException("Enrollment status must be one of " + STATUSES);
        }
        this.enrollmentStatus = status;
    }

    @Override
    public String getRole() {
        return Roles.STUDENT;
    }

    @Override
    public String getExtraData() {
        return enrollmentStatus;
    }

    @Override
    public void loadExtraData(String data) {
        if (data != null && STATUSES.contains(data)) {
            this.enrollmentStatus = data;
        }
    }
}
