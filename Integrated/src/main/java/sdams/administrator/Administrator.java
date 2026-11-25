package sdams.administrator;
import sdams.general.Roles;
import sdams.general.User;

public class Administrator extends User {
    public Administrator(String userID, String name, String contactNum, String passwordHash) {
        super(userID, name, contactNum, passwordHash);
    }

    @Override
    public String getRole() {
        return Roles.ADMINISTRATOR;
    }
}
