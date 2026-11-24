package com.mycompany.assignment;

public abstract class User {
    private String userID;
    private String name;
    private String contactNum;
    private String password;

    protected User(String userID, String name, String contactNum, String passwordHash) {
        this.userID = userID;
        this.name = name;
        this.contactNum = contactNum;
        this.password = passwordHash;
    }

    public String getID() {
        return userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = ValidationUtil.requireName(name);
    }

    public String getContact() {
        return contactNum;
    }

    public void setContact(String contact) {
        this.contactNum = ValidationUtil.requireContact(contact);
    }

    public abstract String getRole();

    public boolean verifyPassword(String password) {
        return PasswordUtil.matches(password, this.password);
    }

    public boolean changePassword(String oldPwd, String newPwd) {
        if (!verifyPassword(oldPwd)) {
            return false;
        }
        ValidationUtil.requirePassword(newPwd);
        this.password = PasswordUtil.hash(newPwd);
        return true;
    }

    public void resetPassword(String newPwd) {
        ValidationUtil.requirePassword(newPwd);
        this.password = PasswordUtil.hash(newPwd);
    }

    String getPasswordHash() {
        return password;
    }

    public String getExtraData() {
        return "";
    }

    public void loadExtraData(String data) {
    }

    @Override
    public String toString() {
        return userID + " - " + name + " (" + Roles.display(getRole()) + ")";
    }
}
