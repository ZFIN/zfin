package org.zfin.profile;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.zfin.framework.api.View;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.framework.presentation.ProvidesLink;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * Domain business object that describes a single person.
 * AccountInfo composite contains login information which is optional.
 */
@Setter
@Getter
public class Person implements UserDetails, Serializable, Comparable<Person>, HasUpdateType, ProvidesLink, HasImage, EntityZdbID {

    @NotNull
    @Size(min = 17, max = 50)
    @JsonView(View.Default.class)
    private String zdbID;

    @NotNull
    @Size(max = 40, message = "Must not be empty and less than 40 characters.")
    @JsonView(View.Default.class)
    private String firstName;

    @NotNull
    @Size(max = 40, message = "Must not be empty and less than 40 characters.")
    @JsonView(View.Default.class)
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

    @JsonView(View.Default.class)
    private String fullName;

    @Size(max = 150, message = "Must not be empty and less than 150 characters.")
    @JsonView(View.Default.class)
    private String email;

    @Size(max = 100, message = "Must not be empty and less than 100 characters.")
    private String fax;

    @Size(max = 19, message = "16 numerals with 3 dashed separating the four-by-four groups")
    private String orcidID;

    private String phone;

    private String address;
    private String country;

    @Size(max = 2000, message = "Must be less than 2000 characters.")
    private String url;

    @Size(max = 12000, message = "Must be less than 12000 characters.")
    private String nonZfinPublications;

    @Size(max = 12000, message = "Must be less than 12000 characters.")
    private String personalBio;

    private Boolean emailList;
    private Boolean deceased = Boolean.FALSE;
    //    private String ownerID;
    private Set<Lab> labs;
    private Set<Company> companies;
    private Set<Publication> publications;
    private AccountInfo accountInfo;
    private String image;
    private boolean hidden;

    private EmailPrivacyPreference emailPrivacyPreference;

    public String getFirstLastName() {
        String ret = "";
        if (firstName != null) {
            ret += firstName;
            ret += " ";
        }
        if (lastName != null)
            ret += lastName;
        return ret;
    }

    public Collection<GrantedAuthority> getAuthorities() {
        if (accountInfo == null) {
            return null;
        }
        String role = accountInfo.getRole();
        GrantedAuthority gr = new SimpleGrantedAuthority(role);
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
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

    public boolean isRootAccount() {
        return accountInfo != null && accountInfo.getRole().equals(AccountInfo.Role.ROOT.toString());
    }

    public static boolean isDeveloper() {
        Person person = ProfileService.getCurrentSecurityUser();
        if (person == null || person.getAccountInfo() == null) {
            return false;
        }
        return person.getAccountInfo().getRole().equals(AccountInfo.Role.ROOT.toString()) && !person.getAccountInfo().isCurator();
    }

    public int hashCode() {
        if (zdbID == null) {
            return getZdbID().hashCode();
        } else {
            return super.hashCode();
        }
    }

    public boolean equals(Object o) {
        if (!(o instanceof Person p)) {
            return false;
        }

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
        String sb = "Person" +
                "{zdbID='" + zdbID + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", name='" + shortName + '\'' +
                ", email='" + email + '\'' +
                '}';
        return sb;
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
            shortName += name.charAt(0) + ".";
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
