package com.mycompany.assignment;

import java.util.regex.Pattern;

public final class ValidationUtil {
    private static final Pattern NAME = Pattern.compile("^[A-Za-z][A-Za-z .'/@-]{1,49}$");

    private static final Pattern PHONE = Pattern.compile("^(\\+?60|0)1\\d{8,9}$");

    private ValidationUtil() {
    }

    public static String requireName(String name) {
        String n = name == null ? "" : name.trim().replaceAll("\\s+", " ");
        if (!NAME.matcher(n).matches()) {
            throw new IllegalArgumentException(
                    "Name must be 2-50 characters, start with a letter and contain only letters, spaces and . ' / @ -");
        }
        return n;
    }

    public static String requireContact(String contact) {
        String c = contact == null ? "" : contact.trim().replaceAll("[\\s-]", "");
        if (!PHONE.matcher(c).matches()) {
            throw new IllegalArgumentException(
                    "Contact must be a Malaysian mobile number, e.g. 012-3456789 or +60123456789");
        }
        return c;
    }

    public static void requirePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        boolean letter = false;
        boolean digit = false;
        for (char ch : password.toCharArray()) {
            if (Character.isLetter(ch)) {
                letter = true;
            }
            if (Character.isDigit(ch)) {
                digit = true;
            }
            if (ch == '|' || ch == '\n' || ch == '\r') {
                throw new IllegalArgumentException("Password cannot contain '|' or line breaks");
            }
        }
        if (!letter || !digit) {
            throw new IllegalArgumentException("Password must contain at least one letter and one digit");
        }
    }

    public static void requireRole(String role) {
        if (!Roles.isValid(role)) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }
}
