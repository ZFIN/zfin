package org.zfin.ontology.presentation;

import org.zfin.mutant.OmimPhenotype;
import org.zfin.ontology.Term;

import java.util.SortedSet;


public class DiseaseDisplay implements Comparable<DiseaseDisplay> {
    private Term diseaseTerm;

    private SortedSet<OmimPhenotype> omimPhenotypes;

    public Term getDiseaseTerm() {
        return diseaseTerm;
    }

    public void setDiseaseTerm(Term diseaseTerm) {
        this.diseaseTerm = diseaseTerm;
    }

    public SortedSet<OmimPhenotype> getOmimPhenotypes() {
        return omimPhenotypes;
    }

    public void setOmimPhenotypes(SortedSet<OmimPhenotype> omimPhenotypes) {
        this.omimPhenotypes = omimPhenotypes;
    }

    public int compareTo(DiseaseDisplay o) {

        if (diseaseTerm == null && o.getDiseaseTerm() == null)  {
            return 1;
        }  else if (diseaseTerm == null && o.getDiseaseTerm() != null)  {
            return 1;
        }  else if (diseaseTerm != null && o.getDiseaseTerm() == null)  {
            return -1;
        }  else {
            return diseaseTerm.compareTo(o.getDiseaseTerm());
        }
    }
}