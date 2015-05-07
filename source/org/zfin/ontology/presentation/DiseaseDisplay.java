package org.zfin.ontology.presentation;

import org.zfin.mutant.OmimPhenotype;
import org.zfin.ontology.Term;

import java.util.SortedSet;


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
        if (diseaseTerm == null)  {
            return 1;
        }  else if (o.getDiseaseTerm() == null)  {
            return -1;
        }  else {
             if (diseaseTerm.compareTo(o.getDiseaseTerm()) == 0) {
                 if (omimTerm.compareToIgnoreCase(o.getOmimTerm()) == 0) {
                     if (omimNumber == null && o.getOmimNumber() != null) {
                         return 1;
                     } else if (omimNumber != null && o.getOmimNumber() == null) {
                         return -1;
                     }
                     return omimNumber.compareTo(o.getOmimNumber());
                 } else {
                     return omimTerm.compareToIgnoreCase(o.getOmimTerm());
                 }
             } else {
                 return diseaseTerm.compareTo(o.getDiseaseTerm());
             }
        }
    }
}