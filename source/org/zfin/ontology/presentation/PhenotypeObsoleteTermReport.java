package org.zfin.ontology.presentation;

import org.zfin.mutant.PhenotypeStatement;

/**
 * Report for phenotypes with obsoleted terms.
 */
public class PhenotypeObsoleteTermReport extends ObsoleteTermReport{

    private PhenotypeStatement phenotypeStatement;

    public PhenotypeObsoleteTermReport(PhenotypeStatement phenotypeStatement) {
        this.phenotypeStatement = phenotypeStatement;
    }

    public PhenotypeStatement getPhenotypeStatement() {
        return phenotypeStatement;
    }
}