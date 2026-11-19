package sdams;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class diagram: UserService (Member 2 owns it).
 * This is a working version covering everything the Manager module needs, so Member 1 can run and
 * test alone. When Member 2's version is ready, keep the method names below and swap the file.
 */
public class UserService {

    /** Lets another member register their own User subclass for a role. */
    public interface UserFactory {
        User create(String userID, String name, String contactNum, String password, String extra);
    }

    private static final Map<String, UserFactory> FACTORIES = new HashMap<>();

    static {
        FACTORIES.put(Roles.MANAGER, (id, n, c, p, x) -> new Manager(id, n, c, p));
        FACTORIES.put(Roles.ADMINISTRATOR, (id, n, c, p, x) -> new Administrator(id, n, c, p));
    }

    /** Example: UserService.registerRole(Roles.STUDENT, (id, n, c, p, x) -> new Student(id, n, c, p, x)); */
    public static void registerRole(String role, UserFactory factory) {
        FACTORIES.put(role, factory);
    }

    private final FileStore store = new FileStore("users.txt");

    // ---- loading and saving -------------------------------------------------
    private List<User> load() {
        List<User> users = new ArrayList<>();
        for (String[] row : store.readAll()) {
            try {
                String extra = row.length > 5 ? row[5] : "";
                UserFactory factory = FACTORIES.get(row[4]);
                users.add(factory != null
                        ? factory.create(row[0], row[1], row[2], row[3], extra)
                        : new GenericUser(row[0], row[1], row[2], row[3], row[4], extra));
            } catch (RuntimeException e) {
                System.err.println("users.txt: skipped bad record \"" + row[0] + "\" (" + e.getMessage() + ")");
            }
        }
        return users;
    }

    private void save(List<User> users) {
        List<String[]> rows = new ArrayList<>();
        for (User u : users) {
            rows.add(u.toRow());
        }
        store.writeAll(rows);
    }

    // ---- lookups ------------------------------------------------------------
    public List<User> getAllUsers() {
        return load();
    }

    /** Case-insensitive ID lookup. Returns null when there is no such user. */
    public User getUserById(String userID) {
        String id = userID == null ? "" : userID.trim();
        for (User u : load()) {
            if (u.getID().equalsIgnoreCase(id)) {
                return u;
            }
        }
        return null;
    }

    /** Next free ID for a role, e.g. AD001, AD002 ... */
    public String generateUserID(String role) {
        String prefix = Roles.idPrefix(role);
        Pattern p = Pattern.compile("^" + prefix + "(\\d+)$", Pattern.CASE_INSENSITIVE);
        int max = 0;
        for (User u : load()) {
            Matcher m = p.matcher(u.getID());
            if (m.matches()) {
                max = Math.max(max, Integer.parseInt(m.group(1)));
            }
        }
        return String.format("%s%03d", prefix, max + 1);
    }

    // ---- methods from the class diagram --------------------------------------
    public void addUser(User user) {
        List<User> users = load();
        for (User u : users) {
            if (u.getID().equalsIgnoreCase(user.getID())) {
                throw new AppException("User ID " + user.getID() + " already exists");
            }
        }
        users.add(user);
        save(users);
    }

    /** Case-insensitive match on ID, name, contact number or role. An empty keyword returns everyone. */
    public List<User> searchUser(String keyword) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        List<User> result = new ArrayList<>();
        for (User u : load()) {
            if (k.isEmpty()
                    || u.getID().toLowerCase(Locale.ROOT).contains(k)
                    || u.getName().toLowerCase(Locale.ROOT).contains(k)
                    || u.getContact().toLowerCase(Locale.ROOT).contains(k)
                    || u.getRole().toLowerCase(Locale.ROOT).contains(k)) {
                result.add(u);
            }
        }
        return result;
    }

    public void updateUser(User user) {
        List<User> users = load();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getID().equals(user.getID())) {
                users.set(i, user);
                save(users);
                return;
            }
        }
        throw new AppException("User " + user.getID() + " not found");
    }

    public void deleteUser(String userID) {
        List<User> users = load();
        if (!users.removeIf(u -> u.getID().equals(userID))) {
            throw new AppException("User " + userID + " not found");
        }
        save(users);
    }

    /** An empty role returns every user. */
    public List<User> viewUsersByRole(String role) {
        List<User> result = new ArrayList<>();
        for (User u : load()) {
            if (role == null || role.isEmpty() || u.getRole().equals(role)) {
                result.add(u);
            }
        }
        return result;
    }

    // ---- staff report -------------------------------------------------------
    /** Staff only (everyone except students), filtered by role and/or part of the name, sorted by role then name. */
    public List<User> viewStaff(String role, String name) {
        if (role != null && !role.isEmpty() && !Roles.STAFF_ROLES.contains(role)) {
            throw new AppException("\"" + role + "\" is not a staff role");
        }
        String n = name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
        List<User> staff = new ArrayList<>();
        for (User u : load()) {
            boolean isStaff = Roles.STAFF_ROLES.contains(u.getRole());
            boolean roleOk = role == null || role.isEmpty() || u.getRole().equals(role);
            boolean nameOk = n.isEmpty() || u.getName().toLowerCase(Locale.ROOT).contains(n);
            if (isStaff && roleOk && nameOk) {
                staff.add(u);
            }
        }
        staff.sort(Comparator.comparing(User::getRole).thenComparing(User::getName));
        return staff;
    }

    /** How many users of each staff role are in the list (every staff role is present, even with 0). */
    public Map<String, Integer> countByRole(List<User> users) {
        Map<String, Integer> counts = new java.util.LinkedHashMap<>();
        for (String r : Roles.STAFF_ROLES) {
            counts.put(r, 0);
        }
        for (User u : users) {
            counts.merge(u.getRole(), 1, Integer::sum);
        }
        return counts;
    }

    /** Diagram signature: generateStaffReport(role, name): String */
    public String generateStaffReport(String role, String name) {
        List<User> staff = viewStaff(role, name);
        StringBuilder sb = new StringBuilder();
        sb.append("STAFF REPORT\n");
        sb.append("Filter: role=").append(role == null || role.isEmpty() ? "ALL" : role)
                .append(", name=").append(name == null || name.isEmpty() ? "ANY" : "\"" + name + "\"").append("\n\n");
        for (User u : staff) {
            sb.append(String.format("%-8s%-26s%-20s%s%n", u.getID(), u.getName(), u.getRole(), u.getContact()));
        }
        sb.append("\nTotal staff: ").append(staff.size()).append("\n");
        for (Map.Entry<String, Integer> e : countByRole(staff).entrySet()) {
            if (e.getValue() > 0) {
                sb.append("  ").append(e.getKey()).append(": ").append(e.getValue()).append("\n");
            }
        }
        return sb.toString();
    }
}
