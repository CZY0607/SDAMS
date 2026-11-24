package sdams;

/** Class diagram: Administrator extends User.  (Member 2 owns this class - replace with theirs.) */
public class Administrator extends User {
    public Administrator(String userID, String name, String contactNum, String password) {
        super(userID, name, contactNum, password);
    }

    @Override
    public String getRole() {
        return Roles.ADMINISTRATOR;
    }
}
