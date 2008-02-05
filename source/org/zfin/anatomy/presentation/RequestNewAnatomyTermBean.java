package org.zfin.anatomy.presentation;

/**
 * Form bean used for requesting a new anatomy structure.
 */
public class RequestNewAnatomyTermBean {

    private String firstname;
    private String lastname;
    private String email;
    private String institution;
    private String termDetail;

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
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
