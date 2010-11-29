package org.zfin.people;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.zfin.publication.Publication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * Domain business object that describes a single person.
 * AccountInfo composite contains login information which is optional.
 */
public class Person implements UserDetails, Serializable, Comparable<Person> {

    private String zdbID;
    private String fullName;
    private String name;
    private String email;
    private String fax;
    private String phone;
    private String address;
    private String url;
    private String nonZfinPublications;
    private String ownerID;
    private boolean emailList;
    private Set<Lab> labs;
    private Set<Publication> publications;
    private AccountInfo accountInfo;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNonZfinPublications() {
        return nonZfinPublications;
    }

    public void setNonZfinPublications(String nonZfinPublications) {
        this.nonZfinPublications = nonZfinPublications;
    }

    public Set<Lab> getLabs() {
        return labs;
    }

    public void setLabs(Set<Lab> labs) {
        this.labs = labs;
    }

    public Set<Publication> getPublications() {
        return publications;
    }

    public void setPublications(Set<Publication> publications) {
        this.publications = publications;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEmailList() {
        return emailList;
    }

    public void setEmailList(boolean emailList) {
        this.emailList = emailList;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }


    public AccountInfo getAccountInfo() {
        return accountInfo;
    }

    public void setAccountInfo(AccountInfo accountInfo) {
        this.accountInfo = accountInfo;
    }

    public Collection<GrantedAuthority> getAuthorities() {
        if (accountInfo == null)
            return null;
        String role = accountInfo.getRole();
        GrantedAuthority gr = new GrantedAuthorityImpl(role);
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>() ;
        grantedAuthorities.add(gr) ;
        return grantedAuthorities ;
    }

    public String getPassword() {
        if (accountInfo == null)
            return null;
        return accountInfo.getPassword();
    }

    public String getUsername() {
        if (accountInfo == null)
            return null;
        return accountInfo.getUsername();
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return true;
    }

    /**
     * This returns a Person object of the current security person.
     * If no authorized Person is found return null.
     *
     * @return Person object
     */
    public static Person getCurrentSecurityUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) {
            return null;
        }
        Authentication authentication = context.getAuthentication();
        if (authentication == null) {
            Person person = new Person();
            person.setName("Guest");
            person.setFullName("Guest User");
            return person;
        }
        Object principal = authentication.getPrincipal();
        // ToDo: Annonymous user should also be a Person object opposed to a String object
        if (principal instanceof String)
            return null;


        // for debugging.  Allows using an in-line spring authentication-manager.
        if(principal instanceof  org.springframework.security.core.userdetails.User) {
            org.springframework.security.core.userdetails.User user =
                    ( org.springframework.security.core.userdetails.User) principal ;
            Person person = new Person();
            person.setName(user.getUsername());
            person.setFullName(user.getUsername());
            return person;
        }

        return (Person) principal;
    }


    /**
     * This returns a Person object of the current security person.
     * If no authorized Person is found return null.
     *
     * @return Is user root?
     */
    public static boolean isCurrentSecurityUserRoot() {
        Person person = getCurrentSecurityUser();
        if (person == null || person.getAccountInfo() == null) {
            return false;
        }
        return person.getAccountInfo().getRole().equals(AccountInfo.Role.ROOT.toString());
    }

    public int hashCode() {
        return getZdbID().hashCode();
    }

    public boolean equals(Object o) {
        if (!(o instanceof Person))
            return false;

        Person p = (Person) o;
        return getZdbID().equals(p.getZdbID());
    }

    public int compareTo(Person anotherPerson) {
        if (getFullName() == null)
            return -1;
        if (anotherPerson.getFullName() == null)
            return +1;
        int personComparison = 0 - getFullName().compareTo(anotherPerson.getFullName());
        if (personComparison != 0)
            return personComparison;

        // in case the 2 persons have the same name?
        // I guess as a tie-breaker we can use the zdbID, which includes date
        return zdbID.compareTo(anotherPerson.getZdbID());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Person");
        sb.append("{zdbID='").append(zdbID).append('\'');
        sb.append(", fullName='").append(fullName).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", email='").append(email).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
