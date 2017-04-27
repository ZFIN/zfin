package org.zfin.profile;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.zfin.feature.Feature;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.marker.Marker;
import org.zfin.mutant.Genotype;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

/**
 *
 */
public abstract class Organization implements Comparable<Organization>, HasUpdateType, HasImage, EntityZdbID {

    public static final String ACTIVE_STATUS = "active";

    // handles both lab and company
    @NotNull
    @Size(min = 16, max = 25)
    private String zdbID;

    @NotNull
    @Size(min = 1, max = 150, message = "Must not be empty and less than 150 characters.")
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

    @Size(max = 150, message = "Must be less than 150 characters.")
//    @Pattern(regexp = "^https?://[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?(:?[0-9]++)?$",
//            message = "Must be of the format http[s]://(domain).(domain)(:port optional)")
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Person getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(Person contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getImage() {
        return image;
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

    public String getName() {
        return name;
    }

    public String getLowerName() {
        return name.toLowerCase();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
        if (!(o instanceof Organization))
            return false;
        Organization lab = (Organization) o;
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


    public Set<Feature> getFeatureSourceList() {
        return featureSourceList;
    }

    public void setFeatureSourceList(Set<Feature> featureSourceList) {
        this.featureSourceList = featureSourceList;
    }

    public Set<Feature> getFeatureSupplierList() {
        return featureSupplierList;
    }

    public void setFeatureSupplierList(Set<Feature> featureSupplierList) {
        this.featureSupplierList = featureSupplierList;
    }

    public Set<Genotype> getGenotypeSourceList() {
        return genotypeSourceList;
    }

    public void setGenotypeSourceList(Set<Genotype> genotypeSourceList) {
        this.genotypeSourceList = genotypeSourceList;
    }

    public Set<Genotype> getGenotypeSupplierList() {
        return genotypeSupplierList;
    }

    public void setGenotypeSupplierList(Set<Genotype> genotypeSupplierList) {
        this.genotypeSupplierList = genotypeSupplierList;
    }

    public Set<Marker> getMarkerSourceList() {
        return markerSourceList;
    }

    public void setMarkerSourceList(Set<Marker> markerSourceList) {
        this.markerSourceList = markerSourceList;
    }

    public Set<Marker> getMarkerSupplierList() {
        return markerSupplierList;
    }

    public void setMarkerSupplierList(Set<Marker> markerSupplierList) {
        this.markerSupplierList = markerSupplierList;
    }

    @Override
    public String getAbbreviation() {
        return name;
    }

    @Override
    public String getAbbreviationOrder() {
        return name;
    }

    public Set<Person> getMemberList() {
        return memberList;
    }

    public void setMemberList(Set<Person> memberList) {
        this.memberList = memberList;
    }

    @Override
    public String getEntityType() {
        return getType();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Organization");
        sb.append("{active=").append(active).append('\'');
        sb.append(", zdbID='").append(zdbID).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", phone='").append(phone).append('\'');
        sb.append(", fax='").append(fax).append('\'');
        sb.append(", email='").append(email).append('\'');
        sb.append(", url='").append(url).append('\'');
        sb.append(", owner=").append(owner).append('\'');
        sb.append(", address='").append(address).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
