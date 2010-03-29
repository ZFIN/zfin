package org.zfin.mutant;

import org.zfin.ontology.Term;
import org.zfin.people.Person;
import org.zfin.publication.Publication;

import java.util.Date;

/**
 * This holds a single phenotype structure, consisting of a superterm (AO), a subterm, a quality term and
 * a tag.
 */
public class PhenotypeStructure implements Comparable<PhenotypeStructure> {

    private String zdbID;
    private Person person;
    private Publication publication;
    private Term superterm;
    private Term subterm;
    private Term quality;
    private Phenotype.Tag tag;
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

    public Term getQuality() {
        return quality;
    }

    public void setQuality(Term quality) {
        this.quality = quality;
    }

    public Phenotype.Tag getTag() {
        return tag;
    }

    public void setTag(Phenotype.Tag tag) {
        this.tag = tag;
    }

    public String getSubtermName() {
        return "";
    }

    public int compareTo(PhenotypeStructure o) {
        if (!(o instanceof PhenotypeStructure))
            throw new RuntimeException("Comparable class not of type PhenotypeStructure");

        if (!superterm.equals(o.getSuperterm()))
            return superterm.getTermName().compareToIgnoreCase(o.getSuperterm().getTermName());
        if (subterm != null && o.getSubterm() == null)
            return 1;
        if (subterm == null && o.getSubterm() != null)
            return -1;
        if (subterm != null && o.getSubterm() != null)
            return subterm.getTermName().compareToIgnoreCase(o.getSubterm().getTermName());
        return quality.getTermName().compareToIgnoreCase(o.getQuality().getTermName());

    }
}