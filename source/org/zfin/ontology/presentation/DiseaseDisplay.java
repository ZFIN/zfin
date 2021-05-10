package org.zfin.ontology.presentation;

import org.zfin.mutant.OmimPhenotype;
import org.zfin.ontology.Term;

public class DiseaseDisplay implements Comparable<DiseaseDisplay> {
    private Term diseaseTerm;
    private OmimPhenotype omimPhenotype;

    public DiseaseDisplay() {
    }

    public Term getDiseaseTerm() {
        return diseaseTerm;
    }

    public void setDiseaseTerm(Term diseaseTerm) {
        this.diseaseTerm = diseaseTerm;
    }

    public OmimPhenotype getOmimPhenotype() {
        return omimPhenotype;
    }

    public void setOmimPhenotype(OmimPhenotype omimPhenotype) {
        this.omimPhenotype = omimPhenotype;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DiseaseDisplay) {
            DiseaseDisplay anotherDiseaseDisplay = (DiseaseDisplay) o;
            if (anotherDiseaseDisplay == null) {
                return false;
            }
            if (anotherDiseaseDisplay.getDiseaseTerm() == null) {
                return anotherDiseaseDisplay.getOmimPhenotype().equals(this.omimPhenotype);
            }
            if (anotherDiseaseDisplay.getDiseaseTerm().getZdbID().equals(this.diseaseTerm.getZdbID())
                    && anotherDiseaseDisplay.getOmimPhenotype().equals(this.omimPhenotype)) {
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
        result = 31 * result + omimPhenotype.getName().hashCode();
        result = 31 * result + omimPhenotype.getOmimNum().hashCode();
        return result;
    }

    public int compareTo(DiseaseDisplay o) {
        OmimPhenotype anotherOmimPhenotype = new OmimPhenotype();
        anotherOmimPhenotype.setOmimNum(o.getOmimPhenotype().getOmimNum());
        anotherOmimPhenotype.setName(o.getOmimPhenotype().getName());
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