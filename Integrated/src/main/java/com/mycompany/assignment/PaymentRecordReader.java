package com.mycompany.assignment;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

public class PaymentRecordReader {
    public static final String PAYMENTS_FILE = "data/payments.txt";
    public static final String REGISTRATIONS_FILE = "data/registrations.txt";

    public static class Row {
        public final String paymentID;
        public final String registrationID;
        public final String studentID;
        public final String studentName;
        public final BigDecimal amount;
        public final LocalDate date;
        public final String method;
        public final String status;

        Row(String paymentID, String registrationID, String studentID, String studentName,
                BigDecimal amount, LocalDate date, String method, String status) {
            this.paymentID = paymentID;
            this.registrationID = registrationID;
            this.studentID = studentID;
            this.studentName = studentName;
            this.amount = amount;
            this.date = date;
            this.method = method;
            this.status = status;
        }
    }

    private final UserService userService;
    private final Path paymentsFile;
    private final Path registrationsFile;

    public PaymentRecordReader(UserService userService) {
        this(userService, PAYMENTS_FILE, REGISTRATIONS_FILE);
    }

    public PaymentRecordReader(UserService userService, String paymentsPath, String registrationsPath) {
        this.userService = userService;
        this.paymentsFile = Paths.get(paymentsPath);
        this.registrationsFile = Paths.get(registrationsPath);
    }

    public List<Row> loadAll() {
        Map<String, String> regToStudent = new HashMap<>();
        for (String[] p : readRows(registrationsFile, 2)) {
            regToStudent.put(p[0].trim(), p[1].trim());
        }
        List<Row> rows = new ArrayList<>();
        for (String[] p : readRows(paymentsFile, 6)) {
            try {
                String regId = p[1].trim();
                int offset = p.length == 7 ? 1 : 0;
                String studentId = offset == 1 ? p[2].trim() : regToStudent.getOrDefault(regId, "?");
                User s = userService.findById(studentId);
                rows.add(new Row(p[0].trim(), regId, studentId, s == null ? "(unknown)" : s.getName(),
                        new BigDecimal(p[2 + offset].trim()), LocalDate.parse(p[3 + offset].trim()),
                        p[4 + offset].trim(), p[5 + offset].trim()));
            } catch (RuntimeException ex) {
            }
        }
        return rows;
    }

    public List<Row> filter(List<Row> all, String keyword, String status, LocalDate from, LocalDate to) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        List<Row> out = new ArrayList<>();
        for (Row r : all) {
            if (!k.isEmpty() && !(r.paymentID.toLowerCase(Locale.ROOT).contains(k)
                    || r.registrationID.toLowerCase(Locale.ROOT).contains(k)
                    || r.studentID.toLowerCase(Locale.ROOT).contains(k)
                    || r.studentName.toLowerCase(Locale.ROOT).contains(k)
                    || r.method.toLowerCase(Locale.ROOT).contains(k))) {
                continue;
            }
            if (status != null && !status.isEmpty() && !r.status.equalsIgnoreCase(status)) {
                continue;
            }
            if (from != null && r.date.isBefore(from)) {
                continue;
            }
            if (to != null && r.date.isAfter(to)) {
                continue;
            }
            out.add(r);
        }
        return out;
    }

    public List<String> distinctStatuses(List<Row> rows) {
        TreeSet<String> set = new TreeSet<>();
        for (Row r : rows) {
            set.add(r.status.toUpperCase(Locale.ROOT));
        }
        return new ArrayList<>(set);
    }

    private static List<String[]> readRows(Path file, int minColumns) {
        List<String[]> out = new ArrayList<>();
        if (!Files.exists(file)) {
            return out;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (line.trim().isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] p = line.split("\\|", -1);
                if (p.length >= minColumns) {
                    out.add(p);
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot read " + file + ": " + ex.getMessage(), ex);
        }
        return out;
    }
}
