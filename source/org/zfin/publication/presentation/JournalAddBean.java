package org.zfin.publication.presentation;

import org.zfin.infrastructure.EntityZdbID;
import org.zfin.infrastructure.SourceAlias;
import org.zfin.publication.Publication;

import java.io.Serializable;
import java.util.Set;
import java.util.SortedSet;

/**
 * Journal domain object.
 */
public class JournalAddBean implements Serializable, EntityZdbID {

    private String zdbID;
    private String name;
    private String abbreviation;

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    private String alias;
    private String printIssn;
    private String eIssn;
    private String nlmID;
    private String publisher;


    private boolean reproduceImages;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String geteIssn() {
        return eIssn;
    }

    public void seteIssn(String eIssn) {
        this.eIssn = eIssn;
    }

    public boolean isReproduceImages() {
        return reproduceImages;
    }

    public void setReproduceImages(boolean reproduceImages) {
        this.reproduceImages = reproduceImages;
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


    public String getPrintIssn() { return printIssn; }

    public void setPrintIssn(String printIssn) { this.printIssn = printIssn; }


    public String getNlmID() { return nlmID; }

    public void setNlmID(String nlmID) { this.nlmID = nlmID; }

}

