package org.zfin.people;

/**
 * Role types for users.
 */
public class UserRole {

    public static final UserRole ROOT = new UserRole("root");
    public static final UserRole SUBMIT = new UserRole("submit");

    private String name;

    private UserRole(String name) {
        this.name = name;
    }

    public UserRole getUserRole(String name){
        return null;
    }
}
