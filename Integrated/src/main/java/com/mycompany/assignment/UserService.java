package com.mycompany.assignment;

import sdams.Manager;
import sdams.student.Student;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class UserService {
    public static final String DEFAULT_FILE = "data/users.txt";
    private static final String DELIM = "|";

    private final Path file;
    private final List<User> users = new ArrayList<>();

    private final java.util.Map<String, Integer> counters = new java.util.HashMap<>();

    public UserService() {
        this(DEFAULT_FILE);
    }

    public UserService(String filePath) {
        this.file = Paths.get(filePath);
        load();
        seedDefaultsIfEmpty();
    }

    public void addUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }
        User.checkUserID(user.getID());
        ValidationUtil.requireRole(user.getRole());
        ValidationUtil.requireName(user.getName());
        ValidationUtil.requireContact(user.getContact());
        if (findById(user.getID()) != null) {
            throw new IllegalArgumentException("User ID already exists: " + user.getID());
        }
        users.add(user);
        noteIssued(user.getID());
        save();
    }

    public User createUser(String role, String name, String contact, String plainPassword, String extra) {
        ValidationUtil.requireRole(role);
        String cleanName = ValidationUtil.requireName(name);
        String cleanContact = ValidationUtil.requireContact(contact);
        ValidationUtil.requirePassword(plainPassword);
        String id = generateId(role);
        String hash = PasswordUtil.hash(plainPassword);
        User u = build(role, id, cleanName, cleanContact, hash);
        applyExtra(u, extra);
        return u;
    }

    public void updateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }
        ValidationUtil.requireName(user.getName());
        ValidationUtil.requireContact(user.getContact());
        int idx = indexOf(user.getID());
        if (idx < 0) {
            throw new IllegalArgumentException("User not found: " + user.getID());
        }
        users.set(idx, user);
        save();
    }

    public void deleteUser(String userID) {
        int idx = indexOf(userID);
        if (idx < 0) {
            throw new IllegalArgumentException("User not found: " + userID);
        }
        User target = users.get(idx);
        if (Roles.MANAGER.equals(target.getRole()) && countByRole(Roles.MANAGER) <= 1) {
            throw new IllegalArgumentException("The last Manager account cannot be deleted");
        }
        users.remove(idx);
        save();
    }

    public User findById(String userID) {
        int idx = indexOf(userID);
        return idx < 0 ? null : users.get(idx);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    public List<User> viewUsersByRole(String role) {
        List<User> out = new ArrayList<>();
        boolean all = role == null || role.isEmpty() || "ALL".equalsIgnoreCase(role);
        for (User u : users) {
            if (all || u.getRole().equalsIgnoreCase(role)) {
                out.add(u);
            }
        }
        out.sort(Comparator.comparing(User::getID));
        return out;
    }

    public List<User> searchUser(String keyword) {
        return searchUser(keyword, null);
    }

    public List<User> searchUser(String keyword, String role) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        List<User> out = new ArrayList<>();
        for (User u : viewUsersByRole(role)) {
            if (k.isEmpty()
                    || u.getID().toLowerCase(Locale.ROOT).contains(k)
                    || u.getName().toLowerCase(Locale.ROOT).contains(k)
                    || u.getContact().contains(k)
                    || u.getRole().toLowerCase(Locale.ROOT).contains(k)
                    || Roles.display(u.getRole()).toLowerCase(Locale.ROOT).contains(k)) {
                out.add(u);
            }
        }
        return out;
    }

    public int countByRole(String role) {
        int n = 0;
        for (User u : users) {
            if (u.getRole().equals(role)) {
                n++;
            }
        }
        return n;
    }

    public String generateStaffReport(String role, String name) {
        String nameKey = name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-8s %-20s %-25s %-15s%n", "ID", "Role", "Name", "Contact"));
        sb.append(repeat('-', 70)).append(System.lineSeparator());
        int total = 0;
        java.util.Map<String, Integer> perRole = new java.util.LinkedHashMap<>();
        for (String r : Roles.ALL_STAFF) {
            perRole.put(r, 0);
        }
        for (User u : viewUsersByRole(role)) {
            if (!Roles.ALL_STAFF.contains(u.getRole())) {
                continue;
            }
            if (!nameKey.isEmpty() && !u.getName().toLowerCase(Locale.ROOT).contains(nameKey)) {
                continue;
            }
            sb.append(String.format("%-8s %-20s %-25s %-15s%n",
                    u.getID(), Roles.display(u.getRole()), u.getName(), u.getContact()));
            perRole.put(u.getRole(), perRole.get(u.getRole()) + 1);
            total++;
        }
        sb.append(repeat('-', 70)).append(System.lineSeparator());
        for (java.util.Map.Entry<String, Integer> e : perRole.entrySet()) {
            sb.append(String.format("Total %-20s: %d%n", Roles.display(e.getKey()), e.getValue()));
        }
        sb.append(String.format("Total staff                : %d%n", total));
        return sb.toString();
    }

    public String generateId(String role) {
        String prefix = Roles.prefixOf(role);
        int max = counters.getOrDefault(prefix, 0);
        for (User u : users) {
            max = Math.max(max, numberOf(u.getID(), prefix));
        }
        return String.format("%s%03d", prefix, max + 1);
    }

    private void noteIssued(String id) {
        for (String role : Roles.ALL) {
            String prefix = Roles.prefixOf(role);
            int n = numberOf(id, prefix);
            if (n > 0) {
                counters.merge(prefix, n, Math::max);
            }
        }
    }

    private static int numberOf(String id, String prefix) {
        if (id == null || !id.startsWith(prefix)) {
            return 0;
        }
        try {
            return Integer.parseInt(id.substring(prefix.length()));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private void load() {
        users.clear();
        counters.clear();
        if (!Files.exists(file)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (line.startsWith("#counter|")) {
                    String[] c = line.split("\\|");
                    if (c.length == 3) {
                        try {
                            counters.put(c[1], Integer.parseInt(c[2]));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    continue;
                }
                if (line.trim().isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] p = line.split("\\|", -1);
                if (p.length < 5 || !Roles.isValid(p[0])) {
                    continue;
                }
                User u = build(p[0], p[1], p[2], p[3], p[4]);
                u.loadExtraData(p.length > 5 ? p[5] : "");
                users.add(u);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot read " + file + ": " + ex.getMessage(), ex);
        }
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        for (java.util.Map.Entry<String, Integer> e : new java.util.TreeMap<>(counters).entrySet()) {
            lines.add("#counter|" + e.getKey() + "|" + e.getValue());
        }
        for (User u : users) {
            lines.add(u.getRole() + DELIM + u.getID() + DELIM + u.getName() + DELIM + u.getContact()
                    + DELIM + u.getPasswordHash() + DELIM + u.getExtraData());
        }
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }

            Path tmp = Paths.get(file.toString() + ".tmp");
            Files.write(tmp, lines, StandardCharsets.UTF_8);
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot write " + file + ": " + ex.getMessage(), ex);
        }
    }

    private void seedDefaultsIfEmpty() {
        if (!users.isEmpty()) {
            return;
        }
        users.add(new Manager("MG001", "Default Manager", "0123456789", PasswordUtil.hash("Manager123")));
        users.add(new Administrator("AD001", "Default Administrator", "0129876543", PasswordUtil.hash("Admin1234")));
        noteIssued("MG001");
        noteIssued("AD001");
        save();
    }

    private int indexOf(String userID) {
        if (userID == null) {
            return -1;
        }
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getID().equalsIgnoreCase(userID.trim())) {
                return i;
            }
        }
        return -1;
    }

    private static User build(String role, String id, String name, String contact, String hash) {
        switch (role) {
            case Roles.MANAGER:            return new Manager(id, name, contact, hash);
            case Roles.ADMINISTRATOR:      return new Administrator(id, name, contact, hash);
            case Roles.STUDENT: return new Student(id, name, contact, hash, "Active");
            case Roles.INSTRUCTOR:
            case Roles.ATTENDANCE_OFFICER:
            case Roles.FACILITIES_OFFICER: return new StaffAccount(role, id, name, contact, hash);
            default: throw new IllegalArgumentException("Unknown role: " + role);
        }
    }

    public static void applyExtra(User u, String extra) {
        if (u instanceof Student) {
            u.loadExtraData(extra);
        } else if (Roles.INSTRUCTOR.equals(u.getRole())) {
            u.loadExtraData(StaffAccount.specializations(extra));
        }
    }

    private static String repeat(char c, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}
