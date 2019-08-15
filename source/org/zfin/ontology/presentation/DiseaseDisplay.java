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

    @Override
    public boolean equals(Object o) {
        if (o instanceof DiseaseDisplay) {
            DiseaseDisplay anotherDiseaseDisplay = (DiseaseDisplay) o;
            if (anotherDiseaseDisplay == null) {
                return false;
            }
            if (anotherDiseaseDisplay.getDiseaseTerm() == null) {
                return anotherDiseaseDisplay.getOmimTerm().equals(this.omimTerm) && anotherDiseaseDisplay.getOmimNumber().equals(this.omimNumber);
            }
            if (anotherDiseaseDisplay.getDiseaseTerm().getZdbID().equals(this.diseaseTerm.getZdbID())
                    && anotherDiseaseDisplay.getOmimTerm().equals(this.omimTerm) && anotherDiseaseDisplay.getOmimNumber().equals(this.omimNumber)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result;
        if (diseaseTerm == null) {
            result = 0;
        } else {
            result = diseaseTerm.getZdbID().hashCode();
        }
        result = 31 * result + omimTerm.hashCode();
        result = 31 * result + omimNumber.hashCode();
        return result;
    }

    public int compareTo(DiseaseDisplay o) {
        OmimPhenotype omimPhenotype = new OmimPhenotype();
        omimPhenotype.setOmimNum(omimNumber);
        omimPhenotype.setName(omimTerm);
        OmimPhenotype anotherOmimPhenotype = new OmimPhenotype();
        anotherOmimPhenotype.setOmimNum(o.getOmimNumber());
        anotherOmimPhenotype.setName(o.getOmimTerm());
        if (o.getDiseaseTerm() == null && diseaseTerm == null) {
            return omimPhenotype.compareTo(anotherOmimPhenotype);
        } else if (diseaseTerm == null) {
            return 1;
        } else if (o.getDiseaseTerm() == null) {
            return -1;
        } else {
            if (diseaseTerm.compareTo(o.getDiseaseTerm()) == 0) {
                return omimPhenotype.compareTo(anotherOmimPhenotype);
            }
            return diseaseTerm.compareTo(o.getDiseaseTerm());
        }
    }
}