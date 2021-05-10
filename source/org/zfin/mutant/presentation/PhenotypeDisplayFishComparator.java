package org.zfin.mutant.presentation;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 */
public class PhenotypeDisplayFishComparator implements Comparator<PhenotypeDisplay> {

    public int compare(PhenotypeDisplay pheno1, PhenotypeDisplay pheno2) {
        if (pheno1 == null && pheno2 == null)
            return 0;
        else if (pheno1 == null)
            return -1;
        else if (pheno2 == null)
            return 1;

        return pheno1.getPhenoStatement().getPhenotypeWarehouse().getFishExperiment().getFish().compareTo(pheno2.getPhenoStatement().getPhenotypeExperiment().getFishExperiment().getFish());
    }
}