package org.zfin.profile;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.zfin.feature.Feature;
import org.zfin.framework.api.View;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.marker.Marker;
import org.zfin.mutant.Genotype;

import java.util.Set;


/**
 *
 */
@Setter
@Getter
public abstract class Organization implements Comparable<Organization>, HasUpdateType, HasImage, EntityZdbID {

    public static final String ACTIVE_STATUS = "active";

    @JsonView(View.API.class)
    private String zdbID;

    @NotNull(message = "Name is required")
    @Size(min = 1, max = 150, message = "Must not be empty and less than 150 characters.")
    @JsonView(View.API.class)
    protected String name;

    @Size(max = 100, message = "Must be less than 100 characters.")
//    @Pattern(regexp = "^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$|^(\\d{3})[\\.](\\d{3})[\\.](\\d{4})$",
//            message = "Must contain only numbers and appropriate punctuation.")
    private String phone;

    @Size(max = 100, message = "Must be less than 100 characters.")
//    @Pattern(regexp = "^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$|^(\\d{3})[\\.](\\d{3})[\\.](\\d{4})$",
//            message = "Must contain only numbers and appropriate punctuation.")
    private String fax;

    @Size(max = 150, message = "Must be less than 150 characters.")
//    @Pattern(regexp = "^[\\w-]+(\\.[\\w-]+)*@([a-z0-9-]+(\\.[a-z0-9-]+)*?\\.[a-z]{2,6}|(\\d{1,3}\\.){3}\\d{1,3})(:\\d{4})?$",
//            message = "Must be of the format (user)@(domain).(domain)")
    private String email;

    @Size(max = 2000, message = "Must be less than 2000 characters.")
    private String url;
    private Person owner;

    //    @Size(max=450,message = "Must be less than 450 characters.")
    private String address;
    private String country;

    private boolean active;
    protected Set<SourceUrl> sourceUrls;
    private String status;

    @Size(max = 12000, message = "Must be less than 12000 characters.")
    private String bio;

    private String image;
    private Person contactPerson;

    private Set<Marker> markerSourceList;
    private Set<Marker> markerSupplierList;

    private Set<Feature> featureSourceList;
    private Set<Feature> featureSupplierList;

    private Set<Genotype> genotypeSourceList;
    private Set<Genotype> genotypeSupplierList;


    private Set<Person> memberList;


    // not sure if this is used . . hopefully not
//    private String contactPersonZdbID ;

    // a non-persisted element, just a convenience
    private String prefix;

    private EmailPrivacyPreference emailPrivacyPreference;

    public String getLowerName() {
        return name.toLowerCase();
    }

    public Set<SourceUrl> getOrganizationUrls() {
        return sourceUrls;
    }

    public void setOrganizationUrls(Set<SourceUrl> sourceUrls) {
        this.sourceUrls = sourceUrls;
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
        if (sourceUrls != null && sourceUrls.size() > 0)
            for (SourceUrl orderURL : sourceUrls) {
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
