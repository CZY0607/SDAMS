package sdams.manager;
import sdams.general.Roles;
import sdams.general.User;

/** Class diagram: Manager extends User. */
public class Manager extends User {
    public Manager(String userID, String name, String contactNum, String passwordHash) {
        super(userID, name, contactNum, passwordHash);
    }

    @Override
    public String getRole() {
        return Roles.MANAGER;
    }
}
