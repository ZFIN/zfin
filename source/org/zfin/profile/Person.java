package org.zfin.profile;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;
import org.zfin.framework.api.View;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.framework.presentation.ProvidesLink;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "person")
public class Person implements UserDetails, Serializable, Comparable<Person>, HasUpdateType, ProvidesLink, HasImage, EntityZdbID {

    @Id
    @NotNull
    @Size(min = 17, max = 50)
    @JsonView(View.Default.class)
    @Column(name = "zdb_id", nullable = false)
    @GeneratedValue(generator = "zdbIdGenerator")
    @GenericGenerator(name = "zdbIdGenerator", strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "PERS"),
                    @org.hibernate.annotations.Parameter(name = "insertActiveSource", value = "true")
            })
    private String zdbID;

    @NotNull
    @Size(max = 40, message = "Must not be empty and less than 40 characters.")
    @JsonView(View.Default.class)
    @Column(name = "first_name")
    private String firstName;

    @NotNull
    @Size(max = 40, message = "Must not be empty and less than 40 characters.")
    @JsonView(View.Default.class)
    @Column(name = "last_name")
    private String lastName;


    // not a hibernate property, just for ease of use
    @Transient
    private String putativeLoginName;

    // not a hibernate property, just for ease of use
    @Transient
    private String pass1;

    @Transient
    private String pass2;

    // not a hibernate property, just for...using person as a formbean, which is kind of wrong
    @Transient
    private String organizationZdbId;

    @Transient
    private Integer position;


    //    @NotNull
    @Size(max = 150, message = "Must not be empty and less than 150 characters.")
    @Column(name = "name")
    private String shortName;

    @JsonView(View.Default.class)
    @Column(name = "full_name")
    private String fullName;

    @Size(max = 150, message = "Must not be empty and less than 150 characters.")
    @JsonView(View.Default.class)
    @Column(name = "email")
    private String email;

    @Size(max = 100, message = "Must not be empty and less than 100 characters.")
    @Column(name = "fax")
    private String fax;

    @Size(max = 19, message = "16 numerals with 3 dashed separating the four-by-four groups")
    @Column(name = "orcid_id")
    private String orcidID;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String address;

    @Column(name = "person_country")
    private String country;

    @Size(max = 2000, message = "Must be less than 2000 characters.")
    @Column(name = "url")
    private String url;

    @Size(max = 12000, message = "Must be less than 12000 characters.")
    @Column(name = "nonzf_pubs")
    private String nonZfinPublications;

    @Size(max = 12000, message = "Must be less than 12000 characters.")
    @Column(name = "pers_bio")
    private String personalBio;

    @Column(name = "on_dist_list")
    private Boolean emailList;

    @Column(name = "pers_is_deceased")
    private Boolean deceased = Boolean.FALSE;

    @Column(name = "image")
    private String image;

    @Column(name = "pers_is_hidden")
    private boolean hidden;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pers_epp_pk_id")
    private EmailPrivacyPreference emailPrivacyPreference;

    //    private String ownerID;

    //TODO: Add translation for <key column="zdb_id"/>?
    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AccountInfo> accountInfoList;

    @ManyToMany
    @JoinTable(
            name = "int_person_lab",
            joinColumns = @JoinColumn(name = "source_id"),
            inverseJoinColumns = @JoinColumn(name = "target_id")
    )
    private Set<Lab> labs = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "int_person_company",
            joinColumns = @JoinColumn(name = "source_id"),
            inverseJoinColumns = @JoinColumn(name = "target_id")
    )
    private Set<Company> companies = new HashSet<>();


    @ManyToMany
    @JoinTable(
            name = "int_person_pub",
            joinColumns = @JoinColumn(name = "source_id"),
            inverseJoinColumns = @JoinColumn(name = "target_id")
    )
    @OrderBy("pub_date desc")
    private Set<Publication> publications = new HashSet<>();

    
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
        if (getAccountInfo() == null) {
            return null;
        }
        String role = getAccountInfo().getRole();
        GrantedAuthority gr = new SimpleGrantedAuthority(role);
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(gr);
        return grantedAuthorities;
    }

    public String getPassword() {
        if (getAccountInfo() == null) {
            return null;
        }
        return getAccountInfo().getPassword();
    }

    public String getUsername() {
        if (getAccountInfo() == null) {
            return null;
        }
        return getAccountInfo().getUsername();
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
        return getAccountInfo() != null;
    }

    public boolean isRootAccount() {
        return getAccountInfo() != null && getAccountInfo().getRole().equals(AccountInfo.Role.ROOT.toString());
    }

    public static boolean isDeveloper() {
        Person person = ProfileService.getCurrentSecurityUser();
        if (person == null || person.getAccountInfo() == null) {
            return false;
        }
        return person.getAccountInfo().getRole().equals(AccountInfo.Role.ROOT.toString()) && !person.getAccountInfo().isCurator();
    }

    public AccountInfo getAccountInfo() {
        return CollectionUtils.isEmpty(accountInfoList) ? null : accountInfoList.iterator().next();
    }

    public void setAccountInfo(AccountInfo accountInfo) {
        if(CollectionUtils.isEmpty(accountInfoList)){
            accountInfoList = new HashSet<>();
        }
        accountInfoList.add(accountInfo);
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
