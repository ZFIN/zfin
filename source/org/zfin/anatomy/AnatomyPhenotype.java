package org.zfin.anatomy;

import org.zfin.mutant.Phenotype;

/**
 * Phenotype that is associated to anatomical structures.
 */
public class AnatomyPhenotype extends Phenotype {

    private AnatomyItem anatomyTerm;


    public AnatomyItem getAnatomyTerm() {
        return anatomyTerm;
    }

    public void setAnatomyTerm(AnatomyItem anatomyTerm) {
        this.anatomyTerm = anatomyTerm;
    }
    
}
