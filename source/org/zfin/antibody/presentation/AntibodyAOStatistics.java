package org.zfin.antibody.presentation;

import org.zfin.antibody.Antibody;
import org.zfin.framework.presentation.AnatomyFact;

/**
 * Class that maps to a statistics table for antibodies
 */
public class AntibodyAOStatistics extends AnatomyFact {

    Antibody antibody;

    public Antibody getAntibody() {
        return antibody;
    }

    public void setAntibody(Antibody antibody) {
        this.antibody = antibody;
    }
}
