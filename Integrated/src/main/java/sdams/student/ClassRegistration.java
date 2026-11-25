package sdams.student;

import java.time.LocalDate;

public class ClassRegistration {
    private String registrationID;
    private String classID;
    private LocalDate bookingDate;
    private String status;
    private String StudentID;

    /**
     * Constructor for ClassRegistration
     * @param registrationID registration ID
     * @param classID class ID
     * @param bookingDate Booking Date
     * @param status Booking status
     * @param StudentID student ID
     */
    public ClassRegistration( String registrationID, String classID, LocalDate bookingDate, String status, String StudentID) {
        //Perform all the validations making sure that all the IDs and statuses and etc
        Validator.checkID(classID, "Class ID", "C");
        Validator.validateStatus(status, "Registration Status", "Booked", "Cancelled");
        Validator.checkID(registrationID, "Registration ID", "R");
        Validator.checkID(StudentID, "Student ID", "S");
        Validator.checkLocalDate(bookingDate, "Booking Date");

        //After validating everything assign the values
        this.registrationID = registrationID;
        this.classID = classID;
        this.bookingDate = bookingDate;
        this.status = status;
        this.StudentID = StudentID;

    }

    /**
     * Updates the status of a registration
     * @param status of the registration
     */
    public void updateStatus(String status) {
        //Validate the status first then a canceled registration cannot be changed. Requires new booking.
        Validator.validateStatus(status, "Registration Status", "Booked", "Cancelled");
        if (this.status.equals("Cancelled")) {
            throw new IllegalArgumentException("Cancelled booking cannot be changed. Please make a new booking.");
        }
        this.status = status;
    }

    /**
     * Retrieves the registration ID
     * @return registration ID
     */
    public String getRegistrationID() {
        return registrationID;
    }

    /**
     * Retrieves the class ID
     * @return class ID
     */
    public String getClassID() {
        return classID;
    }

    /**
     * Retrieves the booking date
     * @return booking date
     */
    public LocalDate getBookingDate() {
        return bookingDate;
    }

    /**
     * Retrieves the booking status
     * @return current booking status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Retrieves the student ID
     * @return student ID
     */
    public String getStudentID() {
        return StudentID;
    }

    /**
     * Retrieves the class ID
     * @param classID class ID
     */
    public void rescheduleClass(String classID) {
        //Validate the ID first before rescheduling the class and changing the status to rescheduled.
        Validator.checkID(classID, "Class ID", "C");
        if (this.classID.equals(classID)) {
            throw new IllegalArgumentException("New class ID must be different from the current one.");
        }
        if (this.status.equals("Cancelled")) {
            throw new IllegalArgumentException("Cancelled class cannot be rescheduled.");
        }

        this.classID = classID;

    }

}
