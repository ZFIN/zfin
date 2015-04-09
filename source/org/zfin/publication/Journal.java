package org.zfin.publication;

import org.zfin.infrastructure.EntityZdbID;

import java.io.Serializable;

/**
 * Journal domain object.
 */
public class Journal implements Serializable, EntityZdbID {

    private String zdbID;
    private String name;
    private String abbreviation;
    private String publisher;

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

}
