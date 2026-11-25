package sdams.student;



import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class PaymentService {



    private static final String PAYMENTS = "payments.txt";

    public PaymentService() {}

    public void makePayment(String registrationID, String studentID, BigDecimal amount, String method) throws IOException, RegistrationException {
        //Validate all the IDs and the payment method.
        Validator.checkID(registrationID, "Registration ID", "R");
        Validator.checkID(studentID, "Student ID", "S");
        Validator.checkAmount(amount, "Payment Amount");
        Validator.validateStatus(method, "Payment Method", "Card", "Cash", "Banking", "E-Wallet");

        //With ClassRegistrationService we search for registrations by registrationID
        ClassRegistrationService registrationService = new ClassRegistrationService();
        ClassRegistration registration = registrationService.findRegistration(registrationID);

        //Checking if a registration belongs to the student before payment
        if (!(registration.getStudentID().equals(studentID))) {
            throw new RegistrationException("Registration " + registrationID + " was not found for " + studentID + ".");
        }

        //Checking if the registration is booked or not as well before payment
        if (!(registration.getStatus().equals("Booked"))) {
            throw new RegistrationException("Registration " + registrationID + " is not booked and cannot be paid.");
        }

        //Load all the payments with private helper function loadAllPayments()
        List<Payment> payments = loadAllPayments();

        //We will go through the payments and check if a registration has been paid, if yes throw exception
        for (Payment payment : payments) {
            if (payment.getRegistrationID().equals(registrationID) && payment.getStatus().equals("Paid")) {
                throw new RegistrationException("Registration " + registrationID+ " has already been paid.");
            }
        }

        //Now we just like bookClass method we load all the current paymentIDs
        List<String> paymentIDs = new ArrayList<>();

        for (Payment payment : payments) {
            paymentIDs.add(payment.getPaymentID());
        }

        //We generate a new paymentID, store it in newPaymentID using the methods from Validator helper class
        String newPaymentID = Validator.generateID(paymentIDs, "P");

        //Save the new payment that has been made
        Payment newPayment = new Payment(
                newPaymentID,
                registrationID,
                studentID,
                amount,
                LocalDate.now(),
                method,
                "Paid"
        );

        payments.add(newPayment);
        saveAllPayments(payments);




    }

    public Payment searchPayment(String paymentID) throws IOException, RegistrationException {
        Validator.checkID(paymentID, "Payment ID", "P");
        List<Payment> currentPayments = loadAllPayments();
        for (Payment payment : currentPayments) {
            if (payment.getPaymentID().equals(paymentID)) {
                return payment;
            }
        }

        throw new RegistrationException("Payment " + paymentID + " not found.");

    }

    public List<Payment> viewPaymentRecords(String studentID) throws IOException {
        Validator.checkID(studentID, "Student ID", "S");
        List<Payment> paymentRecords = loadAllPayments();

        List<Payment> filteredPayments = new ArrayList<>();

        for (Payment payment : paymentRecords) {
            if (payment.getStudentID().equals(studentID)) {
                filteredPayments.add(payment);
            }
        }

        return filteredPayments;
    }

    public String generateRevenueReport(Integer month, Integer year) throws IOException {

        if (year == null || year < 1) {
            throw new IllegalArgumentException("Invalid year. Year must be a positive number.");
        }

        if (month != null && (month < 1 || month > 12)) {
            throw new IllegalArgumentException("Month must be between 1 and 12.");
        }


        List<Payment> payments = loadAllPayments();
        BigDecimal totalRevenue = BigDecimal.ZERO;

        int totalPayments = 0;

        for (Payment payment : payments) {
            boolean yearsMatch = payment.getPaymentDate().getYear() == year;
            boolean monthsMatch = month == null || payment.getPaymentDate().getMonthValue() == month;

            if (yearsMatch && monthsMatch && payment.getStatus().equals("Paid")) {
                totalRevenue = totalRevenue.add(payment.getAmount());
                totalPayments++;
            }
        }

        String reportPeriod;

        if (month == null) {
            reportPeriod = "Year " + year;
        }
        else {
            reportPeriod = month + "/" + year;
        }


        return "Revenue report for " + reportPeriod +"\nTotal Payments: " +
                totalPayments + "\nTotal Revenue: " + totalRevenue;
    }

    private List<Payment> loadAllPayments() throws IOException  {

        List<FileRecord> records = FileHelper.loadAll(PAYMENTS, 7);

        List<Payment> payments = new ArrayList<>();

        for (FileRecord record : records) {
            String[] stringParts = record.getFields();

            try {
                //Each Payment object is nicely placed and returned
                Payment payment = new Payment(
                        stringParts[0], //Payment ID
                        stringParts[1], //Registration ID
                        stringParts[2], //Student ID
                        new BigDecimal(stringParts[3]), //Payment Amount
                        LocalDate.parse(stringParts[4]), //Payment Date
                        stringParts[5], //Payment Method
                        stringParts[6] //Payment Status
                );
                payments.add(payment);


            }

            //When adding payments could result in a DateTimeException catching them is the best option
            catch (IllegalArgumentException | java.time.DateTimeException e) {
                throw new IOException("Invalid payment data on line " + record.getLineNumber() + ": " + e.getMessage(), e);
            }
        }



        return payments;
    }

    private void saveAllPayments(List<Payment> payments) throws IOException {

        List<String> records = new ArrayList<>();
            for (Payment payment : payments) {
                String record = payment.getPaymentID() + "|" +
                            payment.getRegistrationID() + "|" +
                            payment.getStudentID() + "|" +
                            payment.getAmount() + "|" +
                            payment.getPaymentDate() + "|" +
                            payment.getPaymentMethod() + "|" +
                            payment.getStatus();

                records.add(record);

            }
            FileHelper.saveAll(PAYMENTS, records);
    }

    public void refundPayment(String paymentID) throws IOException, RegistrationException{

        Validator.checkID(paymentID, "Payment ID", "P");

        List<Payment> currentPayments = loadAllPayments();

        for (Payment payment : currentPayments) {
            if (payment.getPaymentID().equals(paymentID)) {

                if (payment.getStatus().equals("Refunded")) {
                    throw new RegistrationException("Payment " + paymentID + " is already refunded.");
                }

                if (!payment.getStatus().equals("Paid")) {
                    throw new RegistrationException("Payment " + paymentID + " cannot be refunded.");
                }

                payment.updateStatus("Refunded");
                saveAllPayments(currentPayments);

                return;
            }
        }
        throw new RegistrationException("Payment " + paymentID + " not found.");

    }

    public List<Payment> searchPayments(String studentID, String method, String status, LocalDate startDate,
                                        LocalDate endDate) throws IOException {

        if (studentID != null && !studentID.isBlank()) {
            Validator.checkID((studentID), "Student ID", "S");
        }

        if (method != null && !method.isBlank()) {
            Validator.validateStatus(method, "Payment Method", "Card", "Cash", "Banking", "E-Wallet");
        }

        if (status != null && !status.isBlank()) {
            Validator.validateStatus(status, "Payment Status", "Paid", "Refunded");
        }

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }

        //Load all the payments
        List<Payment> payments = loadAllPayments();

        List<Payment> filteredPayments = new ArrayList<>();

        for (Payment payment : payments) {
            boolean studentMatch = studentID == null || studentID.isBlank() || payment.getStudentID().equals(studentID);
            boolean methodMatch = method == null || method.isBlank() || payment.getPaymentMethod().equals(method);
            boolean statusMatch = status == null || status.isBlank() || payment.getStatus().equals(status);
            boolean startDateMatch = startDate == null || !payment.getPaymentDate().isBefore(startDate);
            boolean endDateMatch = endDate == null || !payment.getPaymentDate().isAfter(endDate);

            if (studentMatch && methodMatch && statusMatch && startDateMatch && endDateMatch) {
                filteredPayments.add(payment);
            }
        }
        return filteredPayments;
    }

    public boolean hasMadePayment(String registrationID) throws IOException{
        Validator.checkID(registrationID, "Registration ID", "R");

        List<Payment> payments = loadAllPayments();

        for (Payment payment : payments) {
            if (payment.getRegistrationID().equals(registrationID) && payment.getStatus().equals("Paid")) {
                return true;
            }
        }

        return false;
    }



}

