package org.zfin.nomenclature;

public abstract class NameSubmission {

    private String name;
    private String email;
    private String email2;
    private String laboratory;
    private String pubStatus;
    private String citations;
    private String comments;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLaboratory() {
        return laboratory;
    }

    public void setLaboratory(String laboratory) {
        this.laboratory = laboratory;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail2() {
        return email2;
    }

    public void setEmail2(String email2) {
        this.email2 = email2;
    }

    public String getPubStatus() {
        return pubStatus;
    }

    public void setPubStatus(String pubStatus) {
        this.pubStatus = pubStatus;
    }

    public String getCitations() {
        return citations;
    }

    public void setCitations(String citations) {
        this.citations = citations;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String toString() {
        return "USER INPUT:\n\n" +
                "Submitter Name: " + name + "\n" +
                "Submitter Email: " + email2 + "\n" +
                "Submitter Lab: " + laboratory + "\n\n" +
                "Publication Status: " + pubStatus + "\n" +
                "Publication Citations: " + citations + "\n" +
                "Comments: " + comments + "\n\n";
    }

    public abstract String getSubjectLine();

}
