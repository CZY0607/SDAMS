package sdams.general;
import sdams.administrator.Administrator;

import java.util.LinkedHashSet;
import java.util.Set;

/** Account details used by Administrator staff CRUD. */
final class StaffAccount extends User {
    private final String role;
    private String extra = "";

    StaffAccount(String role, String id, String name, String contact, String hash) {
        super(id, name, contact, hash);
        this.role = role;
    }

    public String getRole() { return role; }
    public String getExtraData() { return extra; }
    public void loadExtraData(String value) { extra = value == null ? "" : value; }

    static String specializations(String text) {
        Set<String> values = new LinkedHashSet<>();
        for (String part : (text == null ? "" : text).split("[,;]")) {
            String value = part.trim();
            if (value.isEmpty()) continue;
            if (!value.matches("[A-Za-z0-9 &/-]{1,30}")) {
                throw new IllegalArgumentException("Invalid instructor specialization");
            }
            if (values.stream().noneMatch(value::equalsIgnoreCase)) values.add(value);
        }
        return String.join(";", values);
    }
}
