public abstract class User {

    private String userID;
    private String name;
    private String contactNum;
    private String password;

    public User(String userID, String name, String contactNumber, String password) {
        checkPass(password);
        checkUserID(userID);
        checkContact(contactNumber);
        this.userID = userID;
        this.name = validateName(name);
        this.contactNum = contactNumber;
        this.password = password;
    }


    /*
    IMPORTANT VALIDATION RULES (can be changed please discuss with group):
    UserID --> required, role prefix + 3 digits, must be unique. (i.e. Student id --> S001)
    Name --> required, 2-50 characters, allowed letters/apostrophes/spaces/hyphens
    Contact Number --> required, digits only, 6-12 digits valid (we can always change the range)
    Password --> required, 6-30 characters, no spaces allowed
    Enrollment status --> "Active" or "Inactive" (case-sensitive)
    All values are trimmed before validation
    Invalid inputs throw IllegalArgumentException naming the field
    Wrong login password returns false (without any exception)


     */

    /**
     * Abstract method to retrieve the role of the user.
     * @return the role of the user in other classes.
     */
    abstract public String getRole();

    /**
     * Retrieves the user ID
     * @return the user ID
     */
    public String getID() {
        return userID;
    }

    /**
     * Retrieves the name of the user.
     * @return name of the user.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the contact number of the user.
     * @return contact number
     */
    public String getContact() {
        return contactNum;
    }

    /**
     * Sets the name of the user.
     * @param name of the user
     */
    public void setName(String name) {
        this.name = validateName(name);
    }


    /**
     * Sets the contact number
     * @param contact number of the user
     */
    public void setContact(String contact) {
        checkContact(contact);
        this.contactNum = contact;
    }


    /**
     * Verifies the password. Checks if the password is not null.
     *
     * @param password inputted password
     * @return true if the password matches properly.
     *
     * @throw IllegalArgumentException
     */
    public boolean verifyPassword(String password) {
        if (password == null){
            throw new IllegalArgumentException("Password is required.");
        }
        return this.password.equals(password);

    }


    /**
     * Changes the password properly while verifying if the old password matches.
     *
     * @param oldPassword old password of the user
     * @param newPassword new inputted password
     * @return true to if the password change was successful.
     *
     * @throw IllegalArgumentException
     *
     */
    public boolean changePassword(String oldPassword, String newPassword) {

        if (!verifyPassword(oldPassword)) {
            return false;
        }
        checkPass(newPassword);
        this.password = newPassword;

        return true;

    }


    /***
     * Checks the password input specifically for null values, empty values and proper length. Throws proper exceptions based on the outcome.
     * @param input input of the password
     *
     *
     */
    private void checkPass(String input){
        if (input == null) {
            throw new IllegalArgumentException("Password is required");
        }

        if (input.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        if  (!(input.length() >= 6 && input.length() <= 30)) {
            throw new IllegalArgumentException("Password must be 6 to 30 characters.");
        }
        if (input.contains(" ")) {
            throw new IllegalArgumentException("Password cannot contain spaces.");
        }
        if (input.contains("|") || input.contains("\n") || input.contains("\r") || input.contains("\t")) {
            throw new IllegalArgumentException("Password cannot contain |, \\n, \\r, or \\t.");
        }

    }



    /***
     * Checks any input for null values and if the input is blank or empty. Throws proper exception based on the checks.
     * @param input the value of the variable
     * @param label the name of the variable like name.
     */
    protected final void checkInput(String input, String label){
        if  (input == null) {
            throw new IllegalArgumentException(label + " is null, value required.");
        }

        if (input.isBlank()) {
            throw new IllegalArgumentException(label + " is empty, value is required.");
        }
    }

    /**
     * Verifies if the contact number has been inputted properly
     * @param contact contact number to be verified.
     */
    private void checkContact(String contact) {

        //Check for null and empty first before checking for digits
        checkInput(contact, "ContactNumber");
        //Loop through the input and check if each character is a digit
        for (int i = 0; i < contact.length(); i++) {
            if (!Character.isDigit(contact.charAt(i))) {
                //throw and exception if one of the characters is not a digit
                throw new IllegalArgumentException("Contact number must contain digits only");
            }

        }
        if (!(contact.length() >= 6 && contact.length() <= 12)) {
            throw new IllegalArgumentException("Contact number must be 6 to 12 digits.");
        }
    }


    /**
     * Checks if the user id is in proper format.
     * @param ID user id to be verified
     */
    private void checkUserID(String ID) {
        checkInput(ID, "UserID");
        if (!(ID.matches("[A-Z]\\d{3}"))) {
            throw new IllegalArgumentException("UserID must be one upper case letter followed by three digits.");
        }
    }


    /**
     * Checks if the name passed in is valid then restructures the name in proper format.
     * @param name that is inputted
     * @return properly formatted name.
     */
    private String validateName(String name) {
        checkInput(name, "Name");
        String properName = name.trim().replaceAll("\\s+", " ");
        if (!(properName.length() >= 2 && properName.length() <= 50)) {
            throw new IllegalArgumentException("Name must be between 2 and 50 characters");
        }
        boolean foundLetter = false;

        for (int i = 0; i < properName.length(); i++) {
            if (Character.isLetter(properName.charAt(i))) {
                foundLetter = true;
            }
            if (!(Character.isLetter(properName.charAt(i)) || properName.charAt(i) == ' ' || properName.charAt(i) == '\'' || properName.charAt(i) == '-')) {
                throw new IllegalArgumentException("Name contains an invalid character.");

            }

        }
        if (!foundLetter) {
            throw new IllegalArgumentException("Name must contain at least one letter.");
        }

        return properName;


    }







}

