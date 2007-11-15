package org.zfin.people;

/**
 * Created by IntelliJ IDEA.
 * User: Christian Pich
 * Date: Aug 3, 2006
 * Time: 3:24:40 PM
 * To change this template use File | Settings | File Templates.
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
