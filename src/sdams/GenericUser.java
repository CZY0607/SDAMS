package sdams;

/**
 * TEMPORARY stand-in for Student, Instructor, FacilitiesOfficer and AttendanceOfficer, so users.txt can
 * be read before those classes exist. Each owner registers their real class with
 * UserService.registerRole(...) and this class stops being used for that role.
 */
public class GenericUser extends User {
    private final String role;
    private final String extra;

    public GenericUser(String userID, String name, String contactNum, String password, String role, String extra) {
        super(userID, name, contactNum, password);
        this.role = role;
        this.extra = extra == null ? "" : extra;
    }

    @Override
    public String getRole() {
        return role;
    }

    @Override
    public String getExtra() {
        return extra;
    }
}
