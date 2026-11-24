package sdams;

/**
 * Abstract parent of all six user types (class diagram: User).
 * Attributes are private. A subclass only has to implement getRole().
 */
public abstract class User {
    private final String userID;
    private String name;
    private String contactNum;
    private String password;

    protected User(String userID, String name, String contactNum, String password) {
        String id = userID == null ? "" : userID.trim();
        if (!id.matches("[A-Za-z0-9_-]{2,20}")) {
            throw new IllegalArgumentException("User ID must be 2-20 characters (letters, digits, - or _)");
        }
        this.userID = id;
        setName(name);
        setContact(contactNum);
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        this.password = password;
    }

    public String getID() {
        return userID;
    }

    public String getName() {
        return name;
    }

    public final void setName(String name) {
        String n = name == null ? "" : name.trim();
        if (n.length() < 2 || n.length() > 60) {
            throw new IllegalArgumentException("Name must be 2-60 characters");
        }
        this.name = n;
    }

    public String getContact() {
        return contactNum;
    }

    public final void setContact(String contact) {
        String c = contact == null ? "" : contact.trim();
        if (!c.matches("[0-9+\\- ]{7,20}")) {
            throw new IllegalArgumentException("Contact number must be 7-20 digits (+, - and spaces allowed)");
        }
        this.contactNum = c;
    }

    /** Must return one of the six names in Roles. */
    public abstract String getRole();

    /** protected in the class diagram; used by AuthenticationService (same package). */
    protected Boolean verifyPassword(String password) {
        return this.password.equals(password);
    }

    /** protected in the class diagram. */
    protected Boolean changePassword(String oldPwd, String newPwd) {
        if (!verifyPassword(oldPwd)) {
            return false;
        }
        if (newPwd == null || newPwd.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        this.password = newPwd;
        return true;
    }

    /** Role-specific data for the 6th column of users.txt (e.g. an Instructor's specializations). */
    public String getExtra() {
        return "";
    }

    /** For UserService only: the password exactly as it is written to users.txt. */
    String exportPassword() {
        return password;
    }

    /** Row layout of users.txt:  userID|name|contactNum|password|role|extra */
    public String[] toRow() {
        return new String[] {userID, name, contactNum, password, getRole(), getExtra()};
    }
}
