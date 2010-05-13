package org.zfin.mutant;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.ontology.GoTerm;
import org.zfin.ontology.Term;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class ComposedPhenotype extends Phenotype {

    private AnatomyItem anatomySubTerm;
    private GoTerm goSubTerm;


    public AnatomyItem getAnatomySubTerm() {
        return anatomySubTerm;
    }

    public void setAnatomySubTerm(AnatomyItem anatomySubTerm) {
        this.anatomySubTerm = anatomySubTerm;
    }

    public GoTerm getGoSubTerm() {
        return goSubTerm;
    }

    public void setGoSubTerm(GoTerm goSubTerm) {
        this.goSubTerm = goSubTerm;
    }

    @Override
    public Term getSubTerm() {
        if (anatomySubTerm != null)
            return anatomySubTerm;
        if (goSubTerm != null)
            return goSubTerm;
        return null;
    }
}
