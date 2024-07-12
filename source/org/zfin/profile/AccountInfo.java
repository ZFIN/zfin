package org.zfin.profile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Account Info object is a value object of the person object.
 * It contains all the credential-related info, including
 * role and cookie.
 */
@Entity
@Setter
@Getter
@Table(name = "zdb_submitters")
@JsonIgnoreProperties({"accountCreationDate", "cookie", "pass1", "pass2", "password", "previousLoginDate"})
public class AccountInfo implements Serializable {

    @NotNull
    @Column(name = "login", nullable = false)
    private String login;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zdb_id")
    private Person person;

    @Id
    @Column(name = "zdb_id", insertable=false, updatable=false)
    @GeneratedValue(generator = "foreignGenerator")
    @GenericGenerator(name = "foreignGenerator", strategy = "foreign",
            parameters = @org.hibernate.annotations.Parameter(name = "property", value = "person"))
    private String zdbID;
    @Column(name = "password", nullable = false)
    private String password;

    private transient String pass1;
    private transient String pass2;

    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "access", nullable = false)
    private String role;
    @Column(name = "issue_time")
    private Date loginDate;
    @Column(name = "previous_login")
    private Date previousLoginDate;
    @Column(name = "create_date", nullable = false)
    private Date accountCreationDate;
    // ToDo: Only needed as webdatablade integration is needed.
    @Column(name = "cookie")
    private String cookie;
    // Hibernate uses field access to set this variable
    @Column(name = "is_curator")
    private boolean curator;
    @Column(name = "is_student")
    private boolean student;

    @Column(name = "password_reset_key")
    private String passwordResetKey;
    @Column(name = "password_reset_date")
    private Date passwordResetDate;
    @Column(name = "password_last_updated")
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
        if (!(o instanceof AccountInfo accountInfo))
            return false;

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
