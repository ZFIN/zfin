package org.zfin.publication;

import org.zfin.infrastructure.EntityZdbID;
import org.zfin.infrastructure.SourceAlias;

import java.io.Serializable;
import java.util.Set;
import java.util.SortedSet;

/**
 * Journal domain object.
 */
public class Journal implements Serializable, EntityZdbID {

    private String zdbID;
    private String name;
    private String abbreviation;
    private String medAbbrev;
    private String publisher;
    private String printIssn;
    private String onlineIssn;
    private String nlmID;
    private SortedSet<Publication> publications;
    private Set<SourceAlias> aliases;

    private boolean isNice;

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

    public String getAbbreviation() {
        return abbreviation;
    }

    @Override
    public String getAbbreviationOrder() {
        return name;
    }

    @Override
    public String getEntityType() {
        return "Journal";
    }

    @Override
    public String getEntityName() {
        return name;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPrintIssn() { return printIssn; }

    public void setPrintIssn(String printIssn) { this.printIssn = printIssn; }

    public String getOnlineIssn() { return onlineIssn; }

    public void setOnlineIssn(String onlineIssn) { this.onlineIssn = onlineIssn; }


    public String getNlmID() { return nlmID; }

    public void setNlmID(String nlmID) { this.nlmID = nlmID; }

    public boolean isNice() { return isNice; }

    public boolean getIsNice() {
        return isNice;
    }

    public void setIsNice(boolean isNice) {
        this.isNice = isNice;
    }

    public SortedSet<Publication> getPublications() {
        return publications;
    }

    public void setPublications(SortedSet<Publication> publications) {
        this.publications = publications;
    }

    public Set<SourceAlias> getAliases() {
        return aliases;
    }

    public void setAliases(Set<SourceAlias> aliases) {
        this.aliases = aliases;
    }

    public String getMedAbbrev() {
        if (medAbbrev == null)
            return abbreviation;

        return medAbbrev;
    }

    public void setMedAbbrev(String medAbbrev) {
        this.medAbbrev = medAbbrev;
    }

    public boolean isZfinDierectDataSubmission() {
        if (abbreviation.equals("ZFIN Direct Data Submission") || medAbbrev.equals("ZFIN Direct Data Submission"))
            return true;

        return false;
    }
}

