package org.zfin.expression;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.people.Person;
import org.zfin.publication.Publication;

import java.util.Date;

/**
 * This holds a single expression structure, consisting of a superterm (AO), a subterm, a stage in which
 * the superterm is defined and a boolean
 */
public class ExpressionStructure {

    private String zdbID;
    private Person person;
    private Publication publication;
    private AnatomyItem superterm;
    private Date date;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public AnatomyItem getSuperterm() {
        return superterm;
    }

    public void setSuperterm(AnatomyItem superterm) {
        this.superterm = superterm;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
    public String getSubtermID() {
        return subtermID;
    }

    public void setSubtermID(String subtermID) {
        this.subtermID = subtermID;
    }

    private String subtermID;

    public String getSubtermName() {
        return "";
    }

}
