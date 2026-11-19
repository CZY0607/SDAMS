package sdams;

/** Class diagram: Manager extends User. */
public class Manager extends User {
    public Manager(String userID, String name, String contactNum, String password) {
        super(userID, name, contactNum, password);
    }

    @Override
    public String getRole() {
        return Roles.MANAGER;
    }
}
