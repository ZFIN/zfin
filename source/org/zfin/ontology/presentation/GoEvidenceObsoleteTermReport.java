package org.zfin.ontology.presentation;

import org.zfin.mutant.MarkerGoTermEvidence;

/**
 * Report for Go Evidence with obsoleted terms.
 */
public class GoEvidenceObsoleteTermReport extends ObsoleteTermReport{

    private MarkerGoTermEvidence goEvidence;

    public GoEvidenceObsoleteTermReport(MarkerGoTermEvidence goEvidence) {
        this.goEvidence = goEvidence;
    }

    public MarkerGoTermEvidence getGoEvidence() {
        return goEvidence;
    }
}