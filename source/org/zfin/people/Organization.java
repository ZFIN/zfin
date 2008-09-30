package org.zfin.people;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.ObjectUtils;

import java.util.Set;

/**
 *
 */
public abstract class Organization implements Comparable<Organization> {

    private String zdbID;
    protected String name;
    private String phone;
    private String fax;
    private String email;
    private String url;
    private Person owner;
    private String address;
    private String bio;
    private boolean active;
    protected Set<SourceUrl> sourceUrls;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getName() {
        return name;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Set<SourceUrl> getOrganizationUrls() {
        return sourceUrls;
    }

    public void setOrganizationUrls(Set<SourceUrl> sourceUrls) {
        this.sourceUrls = sourceUrls;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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
                StringUtils.equals(address, lab.getAddress()) &&
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

}
