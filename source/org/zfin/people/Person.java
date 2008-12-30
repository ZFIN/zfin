package org.zfin.people;

import org.zfin.publication.Publication;

import java.util.Set;

/**
 * Domain business object that describes a single person: name and address-related info as well
 * as publications. A person is typically created to associate an author to a publications
 * but is also used to for any person providing some service to the community.
 * This class is subclasses.
 */
public class Person {

    protected String zdbID;
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

    public Type getType() {
        return Type.GUEST;
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

    public enum Type{
        GUEST,
        LOGIN
    }

}
