package sdams;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The six role names every module must use. They are written exactly like this in users.txt. */
public final class Roles {
    public static final String MANAGER = "MANAGER";
    public static final String ADMINISTRATOR = "ADMINISTRATOR";
    public static final String STUDENT = "STUDENT";
    public static final String INSTRUCTOR = "INSTRUCTOR";
    public static final String FACILITIES_OFFICER = "FACILITIES_OFFICER";
    public static final String ATTENDANCE_OFFICER = "ATTENDANCE_OFFICER";

    /** Everyone who is not a student counts as staff in the Staff Report. */
    public static final List<String> STAFF_ROLES = Collections.unmodifiableList(
            Arrays.asList(MANAGER, ADMINISTRATOR, INSTRUCTOR, FACILITIES_OFFICER, ATTENDANCE_OFFICER));

    private static final Map<String, String> ID_PREFIX = new LinkedHashMap<>();
    private static final Map<String, String> LABELS = new LinkedHashMap<>();

    static {
        ID_PREFIX.put(MANAGER, "MG");
        ID_PREFIX.put(ADMINISTRATOR, "AD");
        ID_PREFIX.put(STUDENT, "ST");
        ID_PREFIX.put(INSTRUCTOR, "IN");
        ID_PREFIX.put(FACILITIES_OFFICER, "FO");
        ID_PREFIX.put(ATTENDANCE_OFFICER, "AO");

        LABELS.put(MANAGER, "Manager");
        LABELS.put(ADMINISTRATOR, "Administrator");
        LABELS.put(STUDENT, "Student");
        LABELS.put(INSTRUCTOR, "Instructor");
        LABELS.put(FACILITIES_OFFICER, "Facilities Officer");
        LABELS.put(ATTENDANCE_OFFICER, "Attendance Officer");
    }

    private Roles() {
    }

    /** Prefix for generated IDs, e.g. "AD" gives AD001. */
    public static String idPrefix(String role) {
        String prefix = ID_PREFIX.get(role);
        if (prefix == null) {
            throw new IllegalArgumentException("Unknown role: " + role);
        }
        return prefix;
    }

    /** Readable name for the screen, e.g. FACILITIES_OFFICER gives "Facilities Officer". */
    public static String label(String role) {
        return LABELS.getOrDefault(role, role);
    }
}
