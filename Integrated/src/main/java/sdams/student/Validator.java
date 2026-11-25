package sdams.student;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class Validator {

    private Validator() {
    }

    /*
    IMPORTANT HELPER CLASS
    Validator class is a helper class that will have all the useful methods and validation methods and checks
    so other classes that require it can access and use these methods instead of writing these as a private method
    in other methods.
     */

    /***
     * Checks any input for null values and if the input is blank or empty. Throws proper exception based on the checks.
     * @param input the value of the variable
     * @param label the name of the variable like name.
     */
    public static void checkInput(String input, String label) {
        if (input == null) {
            throw new IllegalArgumentException(label + " is null, value required.");
        }

        if (input.isBlank()) {
            throw new IllegalArgumentException(label + " is empty, value is required.");
        }
    }

    public static void checkID(String inputID, String label, String prefix) {

        checkInput(inputID, label);
        if (!(inputID.matches(prefix + "\\d{3}"))) {
            throw new IllegalArgumentException(label + " must contain prefix " + prefix + " followed by 3 digits.");

        }

    }

    public static void checkLocalDate(LocalDate date, String label) {
        if (date == null) {
            throw new IllegalArgumentException(label + " is null, date is required.");
        }
    }

    public static void validateStatus(String value, String label, String... allowedValues) {
        checkInput(value, label);
        for (String option : allowedValues) {
            if (option.equals(value)) {
                return;
            }
        }
        throw new IllegalArgumentException(label + " must be one of " + String.join(", ", allowedValues));

    }

    public static void checkAmount(BigDecimal amount, String label) {
        if (amount == null) {
            throw new IllegalArgumentException(label + " is null, amount is required.");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(label + " must be a positive number.");
        }
        if (amount.scale() > 2) {
            throw new IllegalArgumentException(label + " cannot have more than 2 decimal values");
        }
    }

    public static void checkRange(int range, String label, int min, int max) {
        if (!(range >= min && range <= max)) {
            throw new IllegalArgumentException(label + " must be between " + min + " and " + max + ".");
        }
    }

    public static String checkFreeText(String input, String label) {
        if (input == null) {
            return "";
        }

        String newText = input.trim();
        if (newText.isEmpty()) {
            return "";
        }

        if (newText.length() > 200) {
            throw new IllegalArgumentException(label + " cannot be longer than 200 characters.");
        }

        if (newText.contains("|")) {
            throw new IllegalArgumentException(label + " cannot contain |.");
        }

        if (newText.contains("\n") || newText.contains("\r") || newText.contains("\t")) {
            throw new IllegalArgumentException(label + " cannot contain \\n, \\r, or \\t.");
        }

        return newText;

    }

    public static String generateID(List<String> inputIDs, String prefix) {
        int highest = 0;
        checkInput(prefix, "Prefix");

        if (inputIDs == null) {
            throw new IllegalArgumentException("ID list is required.");
        }

        for (String id : inputIDs) {

            checkID(id, "ID", prefix);

            int number = Integer.parseInt(id.substring(prefix.length()));

            if (number > highest) {
                highest = number;
            }

        }
        int count = highest + 1;

        if (count > 999) {
            throw new IllegalStateException("Maximum allowed number of IDs is 999");
        }

        return prefix + String.format("%03d", count);

    }







}



