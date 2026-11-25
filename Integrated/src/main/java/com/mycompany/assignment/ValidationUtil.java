package com.mycompany.assignment;

public final class ValidationUtil {
    private ValidationUtil() {}

    public static String requireName(String name) {
        return User.validateName(name);
    }

    public static String requireContact(String contact) {
        User.checkContact(contact);
        return contact;
    }

    public static void requirePassword(String password) {
        User.checkPass(password);
    }

    public static void requireRole(String role) {
        if (!Roles.isValid(role)) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }
}
