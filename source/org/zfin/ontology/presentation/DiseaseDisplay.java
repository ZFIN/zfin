package org.zfin.ontology.presentation;

import org.zfin.mutant.OmimPhenotype;
import org.zfin.ontology.Term;

public class DiseaseDisplay implements Comparable<DiseaseDisplay> {
    private Term diseaseTerm;
    private String omimTerm;
    private String omimNumber;

    public DiseaseDisplay() {
    }

    public Term getDiseaseTerm() {
        return diseaseTerm;
    }

    public void setDiseaseTerm(Term diseaseTerm) {
        this.diseaseTerm = diseaseTerm;
    }

    public String getOmimTerm() {
        return omimTerm;
    }

    public void setOmimTerm(String omimTerm) {
        this.omimTerm = omimTerm;
    }

    public String getOmimNumber() {
        return omimNumber;
    }

    public void setOmimNumber(String omimNumber) {
        this.omimNumber = omimNumber;
    }

    public int compareTo(DiseaseDisplay o) {
        if (o.getDiseaseTerm() == null && diseaseTerm == null) {
            return -1;
        } else if (diseaseTerm == null) {
            return 1;
        } else if (o.getDiseaseTerm() == null) {
            return -1;
        } else {
            return diseaseTerm.compareTo(o.getDiseaseTerm());
        }
    }
}