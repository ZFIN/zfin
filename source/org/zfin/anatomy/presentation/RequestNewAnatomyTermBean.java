package org.zfin.anatomy.presentation;

/**
 * Form bean used for requesting a new anatomy structure.
 */
public class RequestNewAnatomyTermBean {

    private String firstName;
    private String lastName;
    private String email;
    private String institution;
    private String termDetail;

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

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getTermDetail() {
        return termDetail;
    }

    public void setTermDetail(String termDetail) {
        this.termDetail = termDetail;
    }
}
