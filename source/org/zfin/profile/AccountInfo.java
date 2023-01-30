package org.zfin.profile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Account Info object is a value object of the person object.
 * It contains all the credential-related info, including
 * role and cookie.
 */
@Setter
@Getter
@JsonIgnoreProperties({"accountCreationDate", "cookie", "pass1", "pass2", "password", "previousLoginDate"})
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
    private boolean student;

    private String passwordResetKey;
    private Date passwordResetDate;
    private Date passwordLastUpdated;

    public boolean getRoot() {
        return role.equals(Role.ROOT.toString());
    }

    public void setRoot(boolean isRoot) {
        role = (isRoot ? Role.ROOT.toString() : Role.SUBMIT.toString());
    }

    public String getUsername() {
        return login;
    }

    public void setPassword(String password) {
        this.password = password;
        this.setPasswordLastUpdated(Calendar.getInstance().getTime());
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
