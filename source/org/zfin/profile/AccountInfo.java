package org.zfin.profile;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * Account Info object is a value object of the person object.
 * It contains all the credential-related info, including
 * role and cookie.
 */
public class AccountInfo implements Serializable {

    private String login;
    private transient String password;

    private transient String pass1;
    private transient String pass2;

    private String name;
    private String role;
    private Date loginDate;
    private Date previousLoginDate;
    private Date accountCreationDate;
    // ToDo: Only needed as webdatablade integration is needed.
    private String cookie;
    // Hibernate uses field access to set this variable
    private String zdbID;
    private boolean curator;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public boolean getRoot() {
        return role.equals(Role.ROOT.toString());
    }

    public void setRoot(boolean isRoot) {
        role = (isRoot ? Role.ROOT.toString() : Role.SUBMIT.toString());
    }

    public String getPassword() {
        return password;
    }

    public String getPass1() {
        return pass1;
    }

    public void setPass1(String pass1) {
        this.pass1 = pass1;
    }

    public String getPass2() {
        return pass2;
    }

    public void setPass2(String pass2) {
        this.pass2 = pass2;
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

    public boolean isCurator() {
        return curator;
    }

    public void setCurator(boolean curator) {
        this.curator = curator;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AccountInfo))
            return false;

        AccountInfo accountInfo = (AccountInfo) o;
        return StringUtils.equals(login, accountInfo.getLogin());
    }

    @Override
    public int hashCode() {
        int hash = 37;
        if (login != null)
            hash += hash * login.hashCode();
        return hash;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public boolean isAdmin() {
        if (role.equals(Role.ROOT.toString())) {
            if (!curator)
                return true;
        }
        return false;
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
