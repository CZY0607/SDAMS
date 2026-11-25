package sdams.general;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class Roles {
    public static final String MANAGER = "MANAGER";
    public static final String ADMINISTRATOR = "ADMINISTRATOR";
    public static final String INSTRUCTOR = "INSTRUCTOR";
    public static final String STUDENT = "STUDENT";
    public static final String FACILITIES_OFFICER = "FACILITIES_OFFICER";
    public static final String ATTENDANCE_OFFICER = "ATTENDANCE_OFFICER";

    public static final List<String> ALL = Collections.unmodifiableList(Arrays.asList(
            MANAGER, ADMINISTRATOR, INSTRUCTOR, STUDENT, FACILITIES_OFFICER, ATTENDANCE_OFFICER));

    public static final List<String> MANAGED_STAFF = Collections.unmodifiableList(Arrays.asList(
            INSTRUCTOR, FACILITIES_OFFICER, ATTENDANCE_OFFICER));

    public static final List<String> ALL_STAFF = Collections.unmodifiableList(Arrays.asList(
            ADMINISTRATOR, INSTRUCTOR, FACILITIES_OFFICER, ATTENDANCE_OFFICER));

    private Roles() {
    }

    public static boolean isValid(String role) {
        return role != null && ALL.contains(role);
    }

    public static String prefixOf(String role) {
        switch (role) {
            case MANAGER:            return "M";
            case ADMINISTRATOR:      return "A";
            case INSTRUCTOR:         return "I";
            case STUDENT:            return "S";
            case FACILITIES_OFFICER: return "F";
            case ATTENDANCE_OFFICER: return "T";
            default: throw new IllegalArgumentException("Unknown role: " + role);
        }
    }

    public static String display(String role) {
        StringBuilder sb = new StringBuilder();
        for (String w : role.split("_")) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(w.charAt(0)).append(w.substring(1).toLowerCase());
        }
        return sb.toString();
    }
}
