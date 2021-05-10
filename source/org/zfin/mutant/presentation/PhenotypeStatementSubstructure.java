package org.zfin.mutant.presentation;

import org.zfin.mutant.PhenotypeStatement;

/**
 * Convenience class to markup Phenotype Statements that are substructures of a given structure.
 */
public class PhenotypeStatementSubstructure {

    private PhenotypeStatement phenotypeStatement;
    boolean substructure;

    public PhenotypeStatementSubstructure(PhenotypeStatement phenotypeStatement, boolean substructure) {
        this.phenotypeStatement = phenotypeStatement;
        this.substructure = substructure;
    }

    public PhenotypeStatement getPhenotypeStatement() {
        return phenotypeStatement;
    }

    public boolean isSubstructure() {
        return substructure;
    }
}
