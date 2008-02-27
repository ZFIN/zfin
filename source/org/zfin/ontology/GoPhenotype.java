package org.zfin.ontology;

import org.zfin.mutant.Phenotype;

/**
 * Phenotype that is associated to a GO term.
 */
public class GoPhenotype extends Phenotype {

    private GoTerm goTerm;


    public GoTerm getGoTerm() {
        return goTerm;
    }

    public void setGoTerm(GoTerm goTerm) {
        this.goTerm = goTerm;
    }

}