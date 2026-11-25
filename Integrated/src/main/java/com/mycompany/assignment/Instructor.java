package com.mycompany.assignment;

import java.util.ArrayList;
import java.util.List;

public class Instructor extends User {
    private final List<String> specializations = new ArrayList<>();

    public Instructor(String userID, String name, String contactNum, String passwordHash) {
        super(userID, name, contactNum, passwordHash);
    }

    public List<String> getSpecializations() {
        return new ArrayList<>(specializations);
    }

    public void addSpecialization(String spec) {
        String s = spec == null ? "" : spec.trim();
        if (s.isEmpty() || !s.matches("[A-Za-z0-9 &/-]{1,30}")) {
            throw new IllegalArgumentException(
                    "Specialization must be 1-30 characters (letters, digits, space, & / -): " + spec);
        }
        for (String e : specializations) {
            if (e.equalsIgnoreCase(s)) {
                return;
            }
        }
        specializations.add(s);
    }

    public void removeSpecialization(String spec) {
        specializations.removeIf(e -> e.equalsIgnoreCase(spec == null ? "" : spec.trim()));
    }

    public void setSpecializationsFromText(String text) {
        Instructor scratch = new Instructor(getID(), getName(), getContact(), getPasswordHash());
        if (text != null) {
            for (String part : text.split(",")) {
                if (!part.trim().isEmpty()) {
                    scratch.addSpecialization(part);
                }
            }
        }
        specializations.clear();
        specializations.addAll(scratch.specializations);
    }

    @Override
    public String getRole() {
        return Roles.INSTRUCTOR;
    }

    @Override
    public String getExtraData() {
        return String.join(";", specializations);
    }

    @Override
    public void loadExtraData(String data) {
        specializations.clear();
        if (data != null && !data.isEmpty()) {
            for (String s : data.split(";")) {
                if (!s.trim().isEmpty()) {
                    specializations.add(s.trim());
                }
            }
        }
    }
}
