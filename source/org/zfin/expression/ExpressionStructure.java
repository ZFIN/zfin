package org.zfin.expression;

import org.zfin.ontology.Term;
import org.zfin.people.Person;
import org.zfin.publication.Publication;

import java.util.Date;

/**
 * This holds a single expression structure, consisting of a superterm (AO), a subterm, a stage in which
 * the superterm is defined and a boolean
 */
public class ExpressionStructure implements Comparable<ExpressionStructure> {

    private Term superterm;
    private Term subterm;
    private String zdbID;
    private Person person;
    private Publication publication;
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Term getSuperterm() {
        return superterm;
    }

    public void setSuperterm(Term superterm) {
        this.superterm = superterm;
    }

    public Term getSubterm() {
        return subterm;
    }

    public void setSubterm(Term subterm) {
        this.subterm = subterm;
    }

    public int compareTo(ExpressionStructure o) {
        if (!(o instanceof ExpressionStructure))
            throw new RuntimeException("Comparable class not of type PhenotypeStructure");

        if (!superterm.equals(o.getSuperterm()))
            return superterm.getTermName().compareToIgnoreCase(o.getSuperterm().getTermName());
        if (subterm != null && o.getSubterm() == null)
            return 1;
        if (subterm == null && o.getSubterm() != null)
            return -1;
        if (subterm != null && o.getSubterm() != null)
            return subterm.getTermName().compareToIgnoreCase(o.getSubterm().getTermName());
        return 0;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExpressionStructure that = (ExpressionStructure) o;

        if (publication != null ? !publication.equals(that.publication) : that.publication != null) return false;
        if (subterm != null ? !subterm.equals(that.subterm) : that.subterm != null) return false;
        if (superterm != null ? !superterm.equals(that.superterm) : that.superterm != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = superterm != null ? superterm.hashCode() : 0;
        result = 31 * result + (subterm != null ? subterm.hashCode() : 0);
        result = 31 * result + (publication != null ? publication.hashCode() : 0);
        return result;
    }
}
