package sdams;

import com.mycompany.assignment.User;
import com.mycompany.assignment.Roles;

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
