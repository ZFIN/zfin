package org.zfin.mutant;

import org.zfin.ontology.GenericTerm;

import java.util.Date;

public class PhenotypeTermFastSearch {

    private long id;
    private PhenotypeStatement phenotypeStatement;
    private GenericTerm term;
    private String tag;
    private Date dateCreated;
    private boolean directAnnotation;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public PhenotypeStatement getPhenotypeStatement() {
        return phenotypeStatement;
    }

    public void setPhenotypeStatement(PhenotypeStatement phenotypeStatement) {
        this.phenotypeStatement = phenotypeStatement;
    }

    public GenericTerm getTerm() {
        return term;
    }

    public void setTerm(GenericTerm term) {
        this.term = term;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public boolean isDirectAnnotation() {
        return directAnnotation;
    }

    public void setDirectAnnotation(boolean directAnnotation) {
        this.directAnnotation = directAnnotation;
    }
}
