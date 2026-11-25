package sdams.student;

import com.mycompany.assignment.User;
import com.mycompany.assignment.Roles;

public class Student extends User {

    private String enrollmentStatus;

    public Student(String userID, String name, String contactNumber, String passwordHash, String enrollmentStatus) {
        super(userID, name, contactNumber, passwordHash);
        Validator.checkID(userID, "Student ID", "S");
        updateEnrollmentStatus(enrollmentStatus);
    }

    public String getEnrollmentStatus() {
        return enrollmentStatus;
    }

    public void updateEnrollmentStatus(String enrollmentStatus) {
        Validator.checkInput(enrollmentStatus, "Enrollment Status");
        Validator.validateStatus(enrollmentStatus, "Enrollment Status", "Active", "Inactive");
        this.enrollmentStatus = enrollmentStatus;
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
    public void loadExtraData(String stored) {
        if ("SUSPENDED".equalsIgnoreCase(stored)) {
            enrollmentStatus = "Suspended"; // Preserve existing disabled accounts.
        } else {
            updateEnrollmentStatus(stored == null || stored.isEmpty() || "ACTIVE".equalsIgnoreCase(stored)
                    ? "Active" : "Inactive");
        }
    }
}
