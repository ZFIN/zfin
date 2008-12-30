package org.zfin.people;

import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.io.Serializable;

/**
 * User object is a value object of the person object.
 * It contains all the credential-related inof, including
 * role and cookie info.
 */
public class User implements Serializable {

    private String zdbID;
    private String login;
    private transient String password;
    private String name;
    private String role;
    private Date loginDate;
    private Date previousLoginDate;
    private Date accountCreationDate;
    // ToDo: Only needed as webdatablade integration is needed.
    private String cookie;
    private Person person;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public Date getLoginDate() {
        return loginDate;
    }

    public void setLoginDate(Date loginDate) {
        this.loginDate = loginDate;
    }

    public Date getPreviousLoginDate() {
        return previousLoginDate;
    }

    public void setPreviousLoginDate(Date previousLoginDate) {
        this.previousLoginDate = previousLoginDate;
    }

    public Date getAccountCreationDate() {
        return accountCreationDate;
    }

    public void setAccountCreationDate(Date accountCreationDate) {
        this.accountCreationDate = accountCreationDate;
    }


    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }


    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User))
            return false;

        User user = (User) o;
        return StringUtils.equals(zdbID, user.getZdbID()) && StringUtils.equals(login, user.getLogin());
    }

    @Override
    public int hashCode() {
        int hash = 37;
        if (zdbID != null)
            hash = hash * zdbID.hashCode();
        if (login != null)
            hash += hash * login.hashCode();
        return hash;
    }

    public enum Role {

        ROOT("root"),
        SUBMIT("submit");

        private String name;

        Role(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }

        public static Role getRole(String type) {
            for (Role role : values()) {
                if (role.toString().equals(type))
                    return role;
            }
            throw new RuntimeException("No user role of string " + type + " found.");
        }
    }
}
