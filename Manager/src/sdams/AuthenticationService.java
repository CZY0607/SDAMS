package sdams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Class diagram: AuthenticationService - login, logout and role-based access. */
public class AuthenticationService {
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_MILLIS = 60_000;

    /** Function names used by hasAccess(). Every module uses these same strings for its menus. */
    private static final Map<String, List<String>> ACCESS = new LinkedHashMap<>();

    static {
        ACCESS.put(Roles.MANAGER, Arrays.asList("MANAGE_ADMINISTRATORS", "STAFF_REPORT", "REVENUE_REPORT"));
        ACCESS.put(Roles.ADMINISTRATOR, Arrays.asList("MANAGE_STAFF", "MANAGE_STUDENTS", "VIEW_PAYMENT_RECORDS"));
        ACCESS.put(Roles.STUDENT, Arrays.asList("MANAGE_BOOKINGS", "MAKE_PAYMENT", "VIEW_PAYMENT_HISTORY", "RATE_INSTRUCTOR"));
        ACCESS.put(Roles.INSTRUCTOR, Arrays.asList("MANAGE_CLASSES", "REPORT_FACILITY_PROBLEM", "VIEW_RATINGS"));
        ACCESS.put(Roles.FACILITIES_OFFICER, Arrays.asList("MANAGE_EQUIPMENT", "MANAGE_FACILITY_PROBLEMS", "EQUIPMENT_STOCK_REPORT"));
        ACCESS.put(Roles.ATTENDANCE_OFFICER, Arrays.asList("RECORD_ATTENDANCE", "MANAGE_ATTENDANCE", "ATTENDANCE_SUMMARY"));
    }

    private final UserService userService;
    private User currentUser;
    private final Map<String, int[]> failedAttempts = new HashMap<>();     // userID -> {count}
    private final Map<String, Long> lockedUntil = new HashMap<>();         // userID -> time in millis

    public AuthenticationService(UserService userService) {
        this.userService = userService;
    }

    /**
     * @return the signed-in user
     * @throws IllegalArgumentException on a wrong ID or password
     * @throws IllegalStateException during a temporary lockout
     */
    public User login(String userID, String password) {
        String key = userID == null ? "" : userID.trim().toLowerCase(Locale.ROOT);
        Long until = lockedUntil.get(key);
        if (until != null && until > System.currentTimeMillis()) {
            throw new IllegalStateException("Too many failed attempts. Wait a minute and try again.");
        }

        User user = userService.getUserById(userID);
        // Same message for "no such user" and "wrong password" so IDs cannot be guessed.
        if (user == null || !user.verifyPassword(password == null ? "" : password)) {
            int[] count = failedAttempts.computeIfAbsent(key, k -> new int[1]);
            count[0]++;
            if (count[0] >= MAX_FAILED_ATTEMPTS) {
                lockedUntil.put(key, System.currentTimeMillis() + LOCK_MILLIS);
                count[0] = 0;
            }
            throw new IllegalArgumentException("Incorrect user ID or password");
        }
        failedAttempts.remove(key);
        lockedUntil.remove(key);
        currentUser = user;
        return user;
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    /** True when the user's role is allowed to use the named function. */
    public Boolean hasAccess(User user, String function) {
        return user != null && ACCESS.getOrDefault(user.getRole(), new ArrayList<>()).contains(function);
    }
}
