package org.zfin.nomenclature.presentation;

public class Nomenclature {

    private String comments;
    private String reason;
    private String name;
    private String abbreviation;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isGeneAbbreviationChange() {
        return abbreviation != null;
    }

    public boolean isGeneNameChange() {
        return name != null;
    }
}
