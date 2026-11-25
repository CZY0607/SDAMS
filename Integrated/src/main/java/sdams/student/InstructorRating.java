package sdams.student;


import java.time.LocalDate;

public class InstructorRating {

    private String RatingID;
    private String InstructorID;
    private String StudentID;
    private String ClassID;
    private int score;
    private String feedback;
    private LocalDate ratingDate;

    /**
     * Instructor rating constructor for instructor ratings
     * @param RatingID rating ID
     * @param InstructorID instructor ID
     * @param StudentID student ID
     * @param ClassID class ID
     * @param score score of the rating
     * @param feedback feedback
     * @param ratingDate date of the rating
     */
    public InstructorRating(String RatingID, String InstructorID, String StudentID,
                            String ClassID, int score, String feedback, LocalDate ratingDate) {

        Validator.checkID(RatingID, "Rating ID", "T");
        Validator.checkID(InstructorID, "Instructor ID", "I");
        Validator.checkID(StudentID, "Student ID", "S");
        Validator.checkID(ClassID, "Class ID", "C");
        Validator.checkLocalDate(ratingDate, "Rating Date");
        Validator.checkRange(score, "Score", 1, 5);
        Validator.checkFreeText(feedback, "Feedback");

        this.RatingID = RatingID;
        this.InstructorID = InstructorID;
        this.StudentID = StudentID;
        this.ClassID = ClassID;
        this.score = score;
        this.feedback = Validator.checkFreeText(feedback, "Feedback");
        this.ratingDate = ratingDate;


    }

    /**
     * Retrieves the rating ID
     * @return rating ID
     */
    public String getRatingID() {
        return RatingID;
    }

    /**
     * Retrieves the instructor ID
     * @return instructor ID
     */
    public String getInstructorID() {
        return InstructorID;
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
     * @return class ID
     */
    public String getClassID() {
        return ClassID;
    }

    /**
     * Retrieves the rating score
     * @return rating score
     */
    public int getScore() {
        return score;
    }

    /**
     * Retrieves the feedback for instructor
     * @return feedback
     */
    public String getFeedback() {
        return feedback;
    }

    /**
     * Retrieves the date of the rating
     * @return rating date
     */
    public LocalDate getRatingDate() {
        return ratingDate;
    }


}
