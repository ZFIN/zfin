package org.zfin.mutant.presentation;

import org.zfin.mutant.PhenotypeStatement;


public class PhenotypeStatementBean {

    PhenotypeStatement phenotypeStatement;
    Long Id;

    public PhenotypeStatement getPhenotypeStatement() {
        return phenotypeStatement;
    }

    public void setPhenotypeStatement(PhenotypeStatement phenotypeStatement) {
        this.phenotypeStatement = phenotypeStatement;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }
}
