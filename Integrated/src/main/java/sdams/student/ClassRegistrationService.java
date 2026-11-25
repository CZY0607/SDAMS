package sdams.student;


import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ClassRegistrationService {
    private static final String REGISTRATIONS_FILE = "registrations.txt";

    /**
     * Empty constructor since this is a service class
     */
    public ClassRegistrationService() {

    }

    /**
     * Books a class properly for a student with all necessary validations
     * @param studentID student ID
     * @param classID class ID
     * @throws IOException throws an exception since we are opening and closing files
     * @throws RegistrationException proper exception
     */
    public void bookClass(String studentID, String classID) throws IOException, RegistrationException {
        Validator.checkID(studentID, "Student ID", "S");
        Validator.checkID(classID, "Class ID", "C");
        List<ClassRegistration> loadedRegistrations = loadAllRegistrations();

        for (ClassRegistration registration : loadedRegistrations) {
            if (registration.getStudentID().equals(studentID) && registration.getClassID().equals(classID) && registration.getStatus().equals("Booked")) {
                throw new RegistrationException("Student " + studentID + " is already booked for " + classID + ".");
            }

        }

        List<String> currentIDs = new ArrayList<>();

        //First we are going to load all the loaded registrations to the new list currentIDs.
        for (ClassRegistration registration : loadedRegistrations) {
            currentIDs.add(registration.getRegistrationID());
        }

        //Using the method generateID in the Validator helper class, this will generate a new registration ID
        String newRegistrationID = Validator.generateID(currentIDs, "R");

        //Now we can create a new Class Registration after we have generated a registration ID
        LocalDate today = LocalDate.now();
        ClassRegistration newClass = new ClassRegistration(newRegistrationID, classID, today, "Booked", studentID);

        loadedRegistrations.add(newClass);

        saveAllRegistrations(loadedRegistrations);


    }

    /**
     * Cancels a booking with proper validation and checks that are necessary before cancelling.
     * @param registrationID registration ID
     * @param studentID student ID
     * @throws IOException IOException
     * @throws RegistrationException RegistrationException to match the output
     */
    public void cancelBooking(String registrationID, String studentID) throws IOException, RegistrationException {

        Validator.checkID(registrationID, "Registration ID", "R");
        Validator.checkID(studentID, "Student ID", "S");

        List<ClassRegistration> loadedRegistrations = loadAllRegistrations();

        for (ClassRegistration registration : loadedRegistrations) {
            if (registration.getRegistrationID().equals(registrationID) && registration.getStudentID().equals(studentID)) {

                if (registration.getStatus().equals("Cancelled")) {
                    throw new RegistrationException("Registration " + registrationID + " is already cancelled.");
                }

                if (!registration.getStatus().equals("Booked")) {
                    throw new RegistrationException("Registration " + registrationID + " cannot be cancelled.");
                }

                PaymentService paymentService = new PaymentService();

                if (paymentService.hasMadePayment(registrationID)) {
                    throw new RegistrationException("Registration " + registrationID + " has been paid already. Cannot cancel booking.");
                }


                registration.updateStatus("Cancelled");

                saveAllRegistrations(loadedRegistrations);
                return;
            }

        }

        throw new RegistrationException("Registration " + registrationID + " not found.");

    }

    /**
     * Search registrations based on class ID and student ID with proper checks in place.
     * @param classID class ID
     * @param studentID student ID
     * @return an ArrayList of ClassRegistration objects after searching for them
     * @throws IOException IOException
     */
    public List<ClassRegistration> searchRegistration(String classID, String studentID) throws IOException {


        //Both the classID and the studentID is trimmed and cleaned to match the ID format
        String cleanedClassID = (classID == null) ? "" : classID.trim();
        String cleanedStudentID = (studentID == null) ? "" : studentID.trim();

        //After that we validate the cleaned inputs both student and class IDs
        if (!(cleanedClassID.isEmpty())) {
            Validator.checkID(cleanedClassID, "Class ID", "C");
        }

        if (!(cleanedStudentID.isEmpty())) {
            Validator.checkID(cleanedStudentID, "Student ID", "S");
        }


        //Load the registrations in a List
        List<ClassRegistration> loadedRegistrations = loadAllRegistrations();


        //A new and empty list to filter and add the registrations
        List<ClassRegistration> filteredRegistrations = new ArrayList<>();


        //Run the for each loop again the loadedRegistrations
        for (ClassRegistration registration : loadedRegistrations) {

            //Easier boolean check which would simply ignore the null values and filter by that option.
            boolean classIDMatch = cleanedClassID.isBlank() || registration.getClassID().equals(cleanedClassID);
            boolean studentIDMatch = cleanedStudentID.isBlank() || registration.getStudentID().equals(cleanedStudentID);

            if (classIDMatch && studentIDMatch) {
                filteredRegistrations.add(registration);
            }
        }
        return filteredRegistrations;

    }

    /**
     * Views the registrations filtering by student with their student ID
     * @param studentID filters based on student ID
     * @return an ArrayList with ClassRegistration objects filtered by student
     * @throws IOException IOException
     */
    public List<ClassRegistration> viewByStudent(String studentID) throws IOException {
        //Validate the studentID first before viewing it by student
        Validator.checkID(studentID, "Student ID", "S");

        //Load all the registrations into a new loadedRegistrations
        List<ClassRegistration> loadedRegistrations = loadAllRegistrations();

        //Created an empty list to filter and add the registrations for viewing by student
        List<ClassRegistration> filteredRegistrations = new ArrayList<>();
        for (ClassRegistration registration : loadedRegistrations) {
            if (registration.getStudentID().equals(studentID)) {
                filteredRegistrations.add(registration);
            }
        }
        return filteredRegistrations;
    }

    /**
     * Reschedules a class with proper checks and validations in place to avoid any errors
     * @param registrationID registration ID
     * @param studentID student ID
     * @param newClassID new class ID to be rescheduled to
     * @throws IOException IOException
     * @throws RegistrationException RegistrationException
     */
    public void rescheduleClass(String registrationID, String studentID, String newClassID) throws IOException, RegistrationException {
        Validator.checkID(registrationID, "Registration ID", "R");
        Validator.checkID(studentID, "Student ID", "S");
        Validator.checkID(newClassID, "Class ID", "C");

        List<ClassRegistration> registrations = loadAllRegistrations();

        ClassRegistration selectedRegistration = null;

        for (ClassRegistration registration : registrations) {
            if (registration.getRegistrationID().equals(registrationID) && registration.getStudentID().equals(studentID)) {
                selectedRegistration = registration;
                break;
            }
        }

        if (selectedRegistration == null) {
            throw new RegistrationException("Registration " + registrationID + " not found for student " + studentID + ".");
        }

        if (!selectedRegistration.getStatus().equals("Booked")) {
            throw new RegistrationException("Registration " + registrationID + " cannot be rescheduled because it is not booked.");
        }

        for (ClassRegistration registration : registrations) {
            if (registration.getStudentID().equals(studentID) && registration.getClassID().equals(newClassID) &&
                    registration.getStatus().equals("Booked")) {
                throw new RegistrationException("Student " + studentID + " is already booked for " + newClassID + ".");
            }
        }

        PaymentService paymentService = new PaymentService();

        if (paymentService.hasMadePayment(registrationID)) {
            throw new RegistrationException("Registration " + registrationID + " has been paid already and cannot be rescheduled.");
        }

        selectedRegistration.rescheduleClass(newClassID);
        saveAllRegistrations(registrations);


    }

    /**
     * Private helper method to load all registrations from the registrations.txt properly
     * @return an ArrayList of ClassRegistration objects
     * @throws IOException IOException
     */
    private List<ClassRegistration> loadAllRegistrations() throws IOException {

        List<FileRecord> records = FileHelper.loadAll(REGISTRATIONS_FILE, 5);

        List<ClassRegistration> registrations = new ArrayList<>();

        for (FileRecord record : records) {
            String[] stringParts = record.getFields();
            try {
                //Each ClassRegistration object is nicely placed and returned
                //Structure is like reID|studentID|classID|date|status
                ClassRegistration registration = new ClassRegistration(
                        stringParts[0], //Registration ID
                        stringParts[2], //Class ID
                        LocalDate.parse(stringParts[3]), //Booking Date
                        stringParts[4], //Status
                        stringParts[1] //Student ID
                );
                registrations.add(registration);
            }
            //When adding registrations could result in a DateTimeException catching them is the best option
            catch (IllegalArgumentException | java.time.DateTimeException e) {
                throw new IOException("Invalid registration data on line " + record.getLineNumber() + ": " + e.getMessage(), e);
            }

        }

        return registrations;
    }

    /**
     * Private helper method to save all registrations properly into registrations.txt
     * @param newRegistrations new ArrayList with ClassRegistration objects to be saved
     * @throws IOException IOException
     */
    private void saveAllRegistrations(List<ClassRegistration> newRegistrations) throws IOException {
        //Save all the registrations to the registrations file following the proper format.
        List<String> records = new ArrayList<>();
        for (ClassRegistration registration : newRegistrations) {
            String record =
                    registration.getRegistrationID() + "|" +
                            registration.getStudentID() + "|" +
                            registration.getClassID() + "|" +
                            registration.getBookingDate() + "|" +
                            registration.getStatus();
            records.add(record);

        }
        FileHelper.saveAll(REGISTRATIONS_FILE, records);
    }

    /**
     * Finds registrations based on the registration ID
     * @param registrationID registration ID
     * @return an object of ClassRegistration if found
     * @throws IOException IOException
     * @throws RegistrationException RegistrationException
     */
    public ClassRegistration findRegistration(String registrationID) throws IOException, RegistrationException {
        Validator.checkID(registrationID, "Registration ID", "R");

        List<ClassRegistration> loadedRegistrations = loadAllRegistrations();

        for (ClassRegistration registration : loadedRegistrations) {
            if (registration.getRegistrationID().equals(registrationID)) {
                return registration;
            }
        }

        throw new RegistrationException("Registration " + registrationID + " not found.");

    }


}
