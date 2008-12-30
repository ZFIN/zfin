package org.zfin.people;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.userdetails.UserDetails;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * User object inherits the Person attributes and
 * contains all the credential-related info, including
 * role and cookies needed for authentication.
 * It implements the UserDetails interface neede for the Acegi Security library.
 */
//ToDo: The attribute 'name' seems to be superfluous as the person class already
//      has a name attribute. No need for duplication. Check with DB design team. 
public class User extends Person implements Serializable, UserDetails {

    private String login;
    private transient String password;
    private String name;
    private String role;
    private Date loginDate;
    private Date previousLoginDate;
    private Date accountCreationDate;
    // ToDo: Only needed as webdatablade integration is needed.
    private String cookie;

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

    @Override
    public Type getType() {
        return Type.LOGIN;
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

    public GrantedAuthority[] getAuthorities() {
        GrantedAuthority gr = new GrantedAuthorityImpl(role);
        return new GrantedAuthority[]{gr};
    }

    // Not applicable in ZFIN but required for the UserDetails interface
    public boolean isAccountNonExpired() {
        return true;
    }

    // Not applicable in ZFIN but required for the UserDetails interface
    public boolean isAccountNonLocked() {
        return true;
    }

    // Not applicable in ZFIN but required for the UserDetails interface
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // Not applicable in ZFIN but required for the UserDetails interface
    public boolean isEnabled() {
        return true;
    }

    /**
     * This returns a Person object of the current security person.
     * If no authorized Person is found return null.
     *
     * @return Person object
     */
    public static User getCurrentSecurityUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null)
            return null;
        Authentication authentication = context.getAuthentication();
        if (authentication == null)
            return null;
        Object principal = authentication.getPrincipal();
        // ToDo: Annonymous user should also be a Person object opposed to a String object
        if (principal instanceof String)
            return null;
        return (User) principal;
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
