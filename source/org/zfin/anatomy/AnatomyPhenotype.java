package org.zfin.anatomy;

import org.zfin.mutant.Phenotype;
import org.zfin.ontology.Term;

/**
 * Phenotype that is associated to anatomical structures.
 */
public class AnatomyPhenotype extends Phenotype {

    private AnatomyItem anatomySubTerm;


    public AnatomyItem getAnatomySubTerm() {
        return anatomySubTerm;
    }

    public void setAnatomySubTerm(AnatomyItem anatomySubTerm) {
        this.anatomySubTerm = anatomySubTerm;
    }

    @Override
    public Term getSubTerm() {
        return anatomySubTerm;
    }
}
