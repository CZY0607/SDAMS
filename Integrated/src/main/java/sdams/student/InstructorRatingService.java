package sdams.student;


import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class InstructorRatingService {

    private static final String RATINGS = "instructor_ratings.txt";

    private final java.util.function.BiPredicate<String, String> attendanceRecorded;

    public InstructorRatingService() {
        this((studentID, classID) -> false);
    }

    public InstructorRatingService(java.util.function.BiPredicate<String, String> attendanceRecorded) {
        this.attendanceRecorded = java.util.Objects.requireNonNull(attendanceRecorded);
    }

    /**
     * Properly adds a new rating to the current ratings with validations and checks
     * @param studentID student ID
     * @param classID class ID
     * @param instructorID instructor ID
     * @param score rating score
     * @param feedback feedback
     * @throws IOException IOException
     * @throws RegistrationException RegistrationException
     */
    public void addRating(String studentID, String classID, String instructorID, int score, String feedback) throws IOException, RegistrationException {
        Validator.checkID(studentID, "Student ID", "S");
        Validator.checkID(classID, "Class ID", "C");
        Validator.checkID(instructorID, "Instructor ID", "I");
        Validator.checkFreeText(feedback, "Feedback");
        Validator.checkRange(score, "Score", 1, 5);

        ClassRegistrationService classRegistrationService = new ClassRegistrationService();
        List<ClassRegistration> registrations = classRegistrationService.searchRegistration(classID, studentID);


        boolean hasBooked = false;

        for (ClassRegistration registration : registrations) {
            if (registration.getStatus().equals("Booked")) {
                hasBooked = true;
                break;
            }
        }

        if (!hasBooked) {
            throw new RegistrationException("Student " + studentID + " does not have an active booking for class " + classID + ".");
        }

        List<InstructorRating> instructorRatings = loadAllRatings();

        //Verify if the student has already rated this class
        for (InstructorRating rating : instructorRatings) {
            if (rating.getStudentID().equals(studentID) && rating.getClassID().equals(classID)) {
                throw new RegistrationException("Student " + studentID + " has already rated the class " + classID + ".");
            }
        }

        if (!attendanceRecorded.test(studentID, classID)) {
            throw new RegistrationException("Attendance must be recorded before rating this class.");
        }


        //Collect all existing ratingIDs
        List<String> ratingIDs = new ArrayList<>();

        for (InstructorRating rating : instructorRatings) {
            ratingIDs.add(rating.getRatingID());
        }

        //Generate a new ratingID
        String newRatingID = Validator.generateID(ratingIDs, "T");

        //Create a new rating
        InstructorRating newRating = new InstructorRating(newRatingID, instructorID, studentID, classID, score, feedback, LocalDate.now());

        instructorRatings.add(newRating);
        saveAllRatings(instructorRatings);

    }

    /**
     * Searches through the registrations with proper validation and checks
     * @param instructorID instructor ID
     * @param studentID student ID
     * @param classID class ID
     * @return a list of all the ratings
     * @throws IOException IOException
     */
    public List<InstructorRating> searchRating(String instructorID, String studentID, String classID) throws IOException {
        String cleanedInstructorID = instructorID == null ? "": instructorID.trim();
        String cleanedClassID = classID == null ? "": classID.trim();
        String cleanedStudentID = studentID == null ? "": studentID.trim();

        if (!cleanedInstructorID.isEmpty()) {
            Validator.checkID(cleanedInstructorID, "Instructor ID", "I");
        }

        if (!cleanedClassID.isEmpty()) {
            Validator.checkID(cleanedClassID, "Class ID", "C");
        }

        if (!cleanedStudentID.isEmpty()) {
            Validator.checkID(cleanedStudentID, "Student ID", "S");
        }

        List<InstructorRating> instructorRatings = loadAllRatings();

        List<InstructorRating> filteredRatings = new ArrayList<>();

        for (InstructorRating rating : instructorRatings) {
            boolean instructorMatch = cleanedInstructorID.isEmpty() || rating.getInstructorID().equals(cleanedInstructorID);
            boolean classMatch = cleanedClassID.isEmpty() || rating.getClassID().equals(cleanedClassID);
            boolean studentMatch = cleanedStudentID.isEmpty() || rating.getStudentID().equals(cleanedStudentID);

            if (instructorMatch && classMatch && studentMatch) {
                filteredRatings.add(rating);
            }

        }

        return filteredRatings;

    }

    /**
     * Calculates and retrieves the average ratings score
     * @param instructorID instructor ID
     * @return a correctly calculated average score
     * @throws IOException IOException
     */
    public Double getAverageScore(String instructorID) throws IOException {
        Validator.checkID(instructorID, "Instructor ID", "I");

        List<InstructorRating> ratings = loadAllRatings();

        int totalScore = 0;
        int ratingCount = 0;

        for (InstructorRating rating: ratings) {
            if (rating.getInstructorID().equals(instructorID)) {
                totalScore += rating.getScore();
                ratingCount++;
            }
        }

        if (ratingCount == 0) {
            return 0.0;
        }

        return (double) totalScore / ratingCount;


    }

    /**
     * Views the instructor ratings by students only
     * @param studentID student ID
     * @return a list of ratings filtered by student with student IDs
     * @throws IOException IOException
     */
    public List<InstructorRating> viewByStudent(String studentID) throws IOException {
        Validator.checkID(studentID, "Student ID", "S");
        List<InstructorRating> ratings = loadAllRatings();

        List<InstructorRating> filteredRatings = new ArrayList<>();

        for (InstructorRating rating : ratings) {
            if (rating.getStudentID().equals(studentID)) {
                filteredRatings.add(rating);
            }
        }

        return filteredRatings;


    }

    /**
     * Private method that properly loads all the ratings
     * @return a list of ratings
     * @throws IOException IOException
     */
    private List<InstructorRating> loadAllRatings() throws IOException {

        List<FileRecord> records = FileHelper.loadAll(RATINGS, 7);

        List<InstructorRating> ratings = new ArrayList<>();

        for (FileRecord record : records) {
            String[] stringParts = record.getFields();
            try {
                //Each Instructor object is nicely placed and returned
                InstructorRating rating = new InstructorRating(
                        stringParts[0], //Rating ID
                        stringParts[1], //Instructor ID
                        stringParts[2], //Student ID
                        stringParts[3], //Class ID
                        Integer.parseInt(stringParts[4]), //Score
                        stringParts[5], //Feedback
                        LocalDate.parse(stringParts[6]) //Rating Date
                );
                ratings.add(rating);
            }

            catch (IllegalArgumentException | java.time.DateTimeException e) {
                throw new IOException("Invalid instructor data on line " + record.getLineNumber() + ": " + e.getMessage(), e);
            }
        }
        return ratings;
    }

    /**
     * Private method to properly save all the ratings into the instructor_ratings.txt
     * @param instructorRating instructor rating list to be saved
     * @throws IOException IOException
     */
    private void saveAllRatings(List<InstructorRating> instructorRating) throws IOException {
        List<String> records = new ArrayList<>();

        for (InstructorRating rating : instructorRating) {
            String record = rating.getRatingID() + "|" +
                    rating.getInstructorID() + "|" +
                    rating.getStudentID() + "|" +
                    rating.getClassID() + "|" +
                    rating.getScore() + "|" +
                    rating.getFeedback() + "|" +
                    rating.getRatingDate();

            records.add(record);
        }

        FileHelper.saveAll(RATINGS, records);

    }






}
