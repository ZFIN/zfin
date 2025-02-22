package org.zfin.profile;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.*;
import org.zfin.feature.Feature;
import org.zfin.framework.api.View;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.marker.Marker;
import org.zfin.mutant.Genotype;

import java.util.Set;


@Setter
@Getter
@MappedSuperclass
public abstract class Organization implements Comparable<Organization>, HasUpdateType, HasImage, EntityZdbID {

    public static final String ACTIVE_STATUS = "active";

    @JsonView(View.API.class)
    @Id
    @Column(name = "zdb_id")
    @GeneratedValue(generator = "zdbIdGenerator")
    @GenericGenerator(name = "zdbIdGenerator", strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = @org.hibernate.annotations.Parameter(name = "insertActiveSource", value = "true"))
    private String zdbID;

    @NotNull(message = "Name is required")
    @Size(min = 1, max = 150, message = "Must not be empty and less than 150 characters.")
    @JsonView(View.API.class)
    @Column(name = "name")
    protected String name;

    @Size(max = 100, message = "Must be less than 100 characters.")
//    @Pattern(regexp = "^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$|^(\\d{3})[\\.](\\d{3})[\\.](\\d{4})$",
//            message = "Must contain only numbers and appropriate punctuation.")
    @Column(name = "phone")
    private String phone;

    @Size(max = 100, message = "Must be less than 100 characters.")
//    @Pattern(regexp = "^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$|^(\\d{3})[\\.](\\d{3})[\\.](\\d{4})$",
//            message = "Must contain only numbers and appropriate punctuation.")
    @Column(name = "fax")
    private String fax;

    @Size(max = 150, message = "Must be less than 150 characters.")
//    @Pattern(regexp = "^[\\w-]+(\\.[\\w-]+)*@([a-z0-9-]+(\\.[a-z0-9-]+)*?\\.[a-z]{2,6}|(\\d{1,3}\\.){3}\\d{1,3})(:\\d{4})?$",
//            message = "Must be of the format (user)@(domain).(domain)")
    @Column(name = "email")
    private String email;

    @Size(max = 2000, message = "Must be less than 2000 characters.")
    @Column(name = "url")
    private String url;
    private Person owner;

    //    @Size(max=450,message = "Must be less than 450 characters.")
    @Column(name = "address")
    private String address;
    @Column(name = "country")
    private String country;

    private boolean active;

    @JoinColumn(name = "srcurl_source_zdb_id")
    @OneToMany
    protected Set<SourceUrl> organizationUrls;

    @Column(name = "status")
    private String status;

    @Size(max = 12000, message = "Must be less than 12000 characters.")
    @Column(name = "bio")
    private String bio;

    @Column(name = "image")

    private String image;
    @ManyToOne
    @JoinColumn(name = "contact_person")
    private Person contactPerson;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "int_data_source",
            joinColumns = @JoinColumn(name = "ids_source_zdb_Id"),
            inverseJoinColumns = @JoinColumn(name = "ids_data_zdb_id")
    )
    private Set<Marker> markerSourceList;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "int_data_supplier",
            joinColumns = @JoinColumn(name = "idsup_supplier_zdb_Id"),
            inverseJoinColumns = @JoinColumn(name = "idsup_data_zdb_id")
    )
    private Set<Marker> markerSupplierList;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "int_data_source", joinColumns = @JoinColumn(name = "ids_source_zdb_Id"), inverseJoinColumns = @JoinColumn(name = "ids_data_zdb_id"))
    private Set<Feature> featureSourceList;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "int_data_supplier", joinColumns = @JoinColumn(name = "idsup_supplier_zdb_Id"), inverseJoinColumns = @JoinColumn(name = "idsup_data_zdb_id"))
    private Set<Feature> featureSupplierList;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "int_data_source", joinColumns = @JoinColumn(name = "ids_source_zdb_Id"), inverseJoinColumns = @JoinColumn(name = "ids_data_zdb_id"))
    private Set<Genotype> genotypeSourceList;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "int_data_supplier", joinColumns = @JoinColumn(name = "idsup_supplier_zdb_Id"), inverseJoinColumns = @JoinColumn(name = "idsup_data_zdb_id"))
    private Set<Genotype> genotypeSupplierList;


    // not sure if this is used . . hopefully not
//    private String contactPersonZdbID ;

    // a non-persisted element, just a convenience
    private String prefix;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "epp_pk_id")
    private EmailPrivacyPreference emailPrivacyPreference;

    public String getLowerName() {
        return name.toLowerCase();
    }

    public boolean isActive() {
        return status.equals(ACTIVE_STATUS);
    }


    @Override
    public String getEntityName() {
        return name;
    }

    /**
     * Retrieve a particular URL, the order url.
     *
     * @return OrganziationUrl
     */
    public SourceUrl getOrganizationOrderURL() {
        if (organizationUrls != null && organizationUrls.size() > 0)
            for (SourceUrl orderURL : organizationUrls) {
                if (SourceUrl.BusinessPurpose.ORDER_THIS.toString().equals(orderURL.getBusinessPurpose()))
                    return orderURL;
            }
        return null;
    }


    public boolean equals(Object o) {
        if (!(o instanceof Organization lab))
            return false;
        return
                ObjectUtils.equals(address, lab.getAddress()) &&
                        StringUtils.equals(email, lab.getEmail()) &&
                        StringUtils.equals(fax, lab.getFax()) &&
                        StringUtils.equals(name, lab.getName()) &&
                        ObjectUtils.equals(owner, lab.getOwner()) &&
                        StringUtils.equals(phone, lab.getPhone()) &&
                        StringUtils.equals(url, lab.getUrl()) &&
                        StringUtils.equals(zdbID, lab.getZdbID());

    }

    public int hashCode() {
        int hash = 37;
        if (address != null)
            hash = hash * address.hashCode();
        if (email != null)
            hash += hash * email.hashCode();
        if (fax != null)
            hash += hash * fax.hashCode();
        if (name != null)
            hash += hash * name.hashCode();
        if (owner != null)
            hash += hash * owner.hashCode();
        if (phone != null)
            hash += hash * phone.hashCode();
        if (url != null)
            hash += hash * url.hashCode();
        if (zdbID != null)
            hash += hash * zdbID.hashCode();
        return hash;
    }

    public abstract boolean getLab();

    public abstract boolean getCompany();


    @Override
    public String getAbbreviation() {
        return name;
    }

    @Override
    public String getAbbreviationOrder() {
        return name;
    }

    @Override
    public String getEntityType() {
        return getType();
    }

    @Override
    public String toString() {
        return "Organization" +
                "{active=" + active + "'" +
                ", zdbID='" + zdbID + "'" +
                ", name='" + name + "'" +
                ", phone='" + phone + "'" +
                ", fax='" + fax + "'" +
                ", email='" + email + "'" +
                ", url='" + url + "'" +
                ", owner=" + owner + "'" +
                ", address='" + address + "'" +
                '}';
    }

//    TODO: Seems like using StringSubstitutor with BeanUtils could simplify this method
//          Uncomment below after including the apache commons text library
//    @Override
//    public String toString() {
//        try {
//            String template = "Organization{active=${active}, zdbID='${zdbID}', name='${name}', phone='${phone}', fax='${fax}', email='${email}', url='${url}', owner=${owner:-null}, address='${address}'}";
//            return StringSubstitutor.replace(template, BeanUtils.describe(this));
//        } catch (Exception e) {
//            return "";
//        }
//    }
}
