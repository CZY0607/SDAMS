public class Student extends User {

    private String enrollmentStatus;

    public Student(String userID, String name, String contactNumber, String password, String enrollmentStatus) {
        super(userID, name, contactNumber, password);
        Validator.checkID(userID, "Student ID", "S");
        Validator.checkInput(enrollmentStatus, "Enrollment Status");
        Validator.validateStatus(enrollmentStatus, "Enrollment Status", "Active", "Inactive");
        this.enrollmentStatus = enrollmentStatus;

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
        return "Student";
    }










}
