package org.zfin.marker.presentation;

import org.zfin.mutant.MarkerGoTermEvidence;

/**
 */
public class GeneOntologyOnMarkerBean {

    int goTermCount ;
    private MarkerGoTermEvidence biologicalProcessEvidence;
    private MarkerGoTermEvidence cellularComponentEvidence;
    private MarkerGoTermEvidence molecularFunctionEvidence;

    public int getGoTermCount() {
        return goTermCount;
    }

    public void setGoTermCount(int goTermCount) {
        this.goTermCount = goTermCount;
    }

    public MarkerGoTermEvidence getBiologicalProcessEvidence() {
        return biologicalProcessEvidence;
    }

    public void setBiologicalProcessEvidence(MarkerGoTermEvidence biologicalProcessEvidence) {
        this.biologicalProcessEvidence = biologicalProcessEvidence;
    }

    public MarkerGoTermEvidence getCellularComponentEvidence() {
        return cellularComponentEvidence;
    }

    public void setCellularComponentEvidence(MarkerGoTermEvidence cellularComponentEvidence) {
        this.cellularComponentEvidence = cellularComponentEvidence;
    }

    public MarkerGoTermEvidence getMolecularFunctionEvidence() {
        return molecularFunctionEvidence;
    }

    public void setMolecularFunctionEvidence(MarkerGoTermEvidence molecularFunctionEvidence) {
        this.molecularFunctionEvidence = molecularFunctionEvidence;
    }
}
