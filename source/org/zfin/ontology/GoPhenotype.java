package org.zfin.ontology;

import org.zfin.mutant.Phenotype;

/**
 * Phenotype that is associated to a GO term.
 */
public class GoPhenotype extends Phenotype {

    private GoTerm goSubTerm;


    public GoTerm getGoSubTerm() {
        return goSubTerm;
    }

    public void setGoSubTerm(GoTerm goSubTerm) {
        this.goSubTerm = goSubTerm;
    }

    @Override
    public Term getSubTerm() {
        return goSubTerm;
    }
}