package sdams.student;


import java.math.BigDecimal;
import java.time.LocalDate;

public class Payment {

    private String paymentID;
    private String registrationID;
    private String StudentID;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String paymentMethod;
    private String status;

    /**
     * Payment constructor that holds proper values for a payment
     * @param paymentID payment ID
     * @param registrationID registration ID
     * @param StudentID student ID
     * @param amount amount to be paid
     * @param paymentDate payment date
     * @param paymentMethod payment method
     * @param status status of the payment
     */
    public Payment(String paymentID, String registrationID, String StudentID,
                   BigDecimal amount, LocalDate paymentDate, String paymentMethod,
                   String status) {
        //Validates everything before all the values are properly assigned.
        Validator.checkID(paymentID, "Payment ID", "P");
        Validator.checkID(registrationID, "Registration ID", "R");
        Validator.checkID(StudentID, "Student ID", "S");
        Validator.checkLocalDate(paymentDate, "Payment Date");
        Validator.validateStatus(paymentMethod, "Payment Method", "Card", "Cash", "Banking", "E-Wallet");
        Validator.validateStatus(status, "Payment Status", "Paid", "Refunded");
        Validator.checkAmount(amount, "Payment Amount");

        //Assign all the proper values.
        this.paymentID = paymentID;
        this.registrationID = registrationID;
        this.StudentID = StudentID;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.status = status;

    }

    /**
     * Retrieves the payment ID
     * @return payment ID
     */
    public String getPaymentID() {
        return paymentID;
    }

    /**
     * Retrieves the registration ID
     * @return registration ID
     */
    public String getRegistrationID() {
        return registrationID;
    }

    /**
     * Retrieves the student ID
     * @return student ID
     */
    public String getStudentID() {
        return StudentID;
    }

    /**
     * Retrieves the amount paid
     * @return payment amount
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Retrieves the payment date
     * @return payment date
     */
    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    /**
     * Updates the status of the payment. Sets the status to refunded as the final status after updated.
     * @param status status of the payment
     */
    public void updateStatus(String status) {
        Validator.validateStatus(status, "Payment Status", "Paid", "Refunded");
        if (this.status.equals("Refunded")) {
            throw new IllegalArgumentException("A refunded payment cannot be changed.");
        }

        this.status = status;
    }

    /**
     * Retrieves the payment method
     * @return payment method
     */
    public String getPaymentMethod() {
        return paymentMethod;
    }

    /**
     * Retrieves the payment status
     * @return payment status
     */
    public String getStatus() {
        return status;
    }


}
