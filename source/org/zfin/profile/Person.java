package org.zfin.profile;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.framework.presentation.ProvidesLink;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * Domain business object that describes a single person.
 * AccountInfo composite contains login information which is optional.
 */
public class Person implements UserDetails, Serializable, Comparable<Person>, HasUpdateType, ProvidesLink, HasImage, EntityZdbID {

    @NotNull
    @Size(min = 17, max = 50)
    private String zdbID;

    @NotNull
    @Size(max = 40, message = "Must not be empty and less than 40 characters.")
    private String firstName;

    @NotNull
    @Size(max = 40, message = "Must not be empty and less than 40 characters.")
    private String lastName;


    // not a hibernate property, just for ease of use
    private String putativeLoginName;

    // not a hibernate property, just for ease of use
    private String pass1;
    private String pass2;

    // not a hibernate property, just for...using person as a formbean, which is kind of wrong
    private String organizationZdbId;
    private Integer position;


    //    @NotNull
    @Size(max = 150, message = "Must not be empty and less than 150 characters.")
    private String shortName;

    private String fullName;

    @Size(max = 150, message = "Must not be empty and less than 150 characters.")
    private String email;

    @Size(max = 100, message = "Must not be empty and less than 100 characters.")
    private String fax;

    @Size(max = 19, message = "16 numerals with 3 dashed separating the four-by-four groups")
    private String orcidID;

    private String phone;

    private String address;
    private String country;

    @Size(max = 150, message = "Must not be empty and less than 150 characters.")
    private String url;

    @Size(max = 12000, message = "Must be less than 12000 characters.")
    private String nonZfinPublications;

    @Size(max = 12000, message = "Must be less than 12000 characters.")
    private String personalBio;

    private Boolean emailList;
    private Boolean deceased = new Boolean(false);
    //    private String ownerID;
    private Set<Lab> labs;
    private Set<Company> companies;
    private Set<Publication> publications;
    private AccountInfo accountInfo;
    private String image;

    public String getPutativeLoginName() {
        return putativeLoginName;
    }

    public void setPutativeLoginName(String putativeLoginName) {
        this.putativeLoginName = putativeLoginName;
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

    public String getOrganizationZdbId() {
        return organizationZdbId;
    }

    public void setOrganizationZdbId(String organizationZdbId) {
        this.organizationZdbId = organizationZdbId;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getImage() {
        return image;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getOrcidID() {
        return orcidID;
    }

    public void setOrcidID(String orcidID) {
        this.orcidID = orcidID;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    public Set<Company> getCompanies() {
        return companies;
    }

    public void setCompanies(Set<Company> companies) {
        this.companies = companies;
    }

    public Set<Publication> getPublications() {
        return publications;
    }

    public void setPublications(Set<Publication> publications) {
        this.publications = publications;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Boolean getEmailList() {
        return emailList;
    }

    public void setEmailList(Boolean emailList) {
        this.emailList = emailList;
    }

    public Boolean getDeceased() {
        return deceased;
    }

    public void setDeceased(Boolean deceased) {
        this.deceased = deceased;
    }

    public String getPersonalBio() {
        return personalBio;
    }

    public void setPersonalBio(String personalBio) {
        this.personalBio = personalBio;
    }

    public AccountInfo getAccountInfo() {
        return accountInfo;
    }

    public void setAccountInfo(AccountInfo accountInfo) {
        this.accountInfo = accountInfo;
    }

    public Collection<GrantedAuthority> getAuthorities() {
        if (accountInfo == null) {
            return null;
        }
        String role = accountInfo.getRole();
        GrantedAuthority gr = new SimpleGrantedAuthority(role);
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
        grantedAuthorities.add(gr);
        return grantedAuthorities;
    }

    public String getPassword() {
        if (accountInfo == null) {
            return null;
        }
        return accountInfo.getPassword();
    }

    public String getUsername() {
        if (accountInfo == null) {
            return null;
        }
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

    public boolean isLoginAccount() {
        return accountInfo != null;
    }

    /**
     * This returns a Person object of the current security person.
     * If no authorized Person is found return null.
     *
     * @return Is user root?
     */
    public static boolean isCurrentSecurityUserRoot() {
        Person person = ProfileService.getCurrentSecurityUser();
        if (person == null || person.getAccountInfo() == null) {
            return false;
        }
        return person.getAccountInfo().getRole().equals(AccountInfo.Role.ROOT.toString());
    }

    public int hashCode() {
        if (zdbID == null) {
            return getZdbID().hashCode();
        } else {
            return super.hashCode();
        }
    }

    public boolean equals(Object o) {
        if (!(o instanceof Person)) {
            return false;
        }

        Person p = (Person) o;
        return getZdbID().equals(p.getZdbID());
    }

    public int compareTo(Person anotherPerson) {
        if (getFullName() == null) {
            return -1;
        }
        if (anotherPerson.getFullName() == null) {
            return +1;
        }
        int personComparison = 0 - getFullName().compareTo(anotherPerson.getFullName());
        if (personComparison != 0) {
            return personComparison;
        }

        // in case the 2 persons have the same name?
        // I guess as a tie-breaker we can use the zdbID, which includes date
        return zdbID.compareTo(anotherPerson.getZdbID());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Person");
        sb.append("{zdbID='").append(zdbID).append('\'');
        sb.append(", fullName='").append(getFullName()).append('\'');
        sb.append(", name='").append(shortName).append('\'');
        sb.append(", email='").append(email).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public String getType() {
        return "person";
    }

    @Override
    public String getLink() {
        return EntityPresentation.getViewLink(zdbID, getFullName(), null, "person-link");
    }

    @Override
    public String getLinkWithAttribution() {
        return getLink();
    }

    @Override
    public String getLinkWithAttributionAndOrderThis() {
        return getLink();
    }


    public void generateNameVariations() {
        String shortName = getLastName() + "-";

        String[] firstNames = getFirstName().split(" ");

        for (String name : firstNames) {
            shortName += name.substring(0, 1) + ".";
        }
        setShortName(shortName);

        setFullName(lastName + ", " + firstName);
    }

    @Override
    public String getAbbreviation() {
        return lastName;
    }

    @Override
    public String getAbbreviationOrder() {
        return lastName;
    }

    @Override
    public String getEntityType() {
        return "Person";
    }

    @Override
    public String getEntityName() {
        return fullName;
    }

    public String getDisplay() {
        return firstName + " " + lastName;
    }
}
