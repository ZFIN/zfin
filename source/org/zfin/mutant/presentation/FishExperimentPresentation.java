package org.zfin.mutant.presentation;

import org.apache.log4j.Logger;
import org.zfin.expression.presentation.ExperimentPresentation;
import org.zfin.fish.presentation.FishPresentation;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.mutant.FishExperiment;

/**
 *
 * This class is used to populate elements under Fish column in figure view's tables
 * Previously, when fish was absent, the experiment would still show up, and we wanted to avoid that
 *
 * Notice that in expressionTable.tag, <zfin:link entity="${row.genotypeExperiment}"/> was originally
 * <zfin:link entity="${row.genotype}"/> &nbsp;
 * <zfin:link entity="${row.experiment}"/>
 *
 */
public class FishExperimentPresentation extends EntityPresentation {

    private static Logger logger = Logger.getLogger(FishExperimentPresentation.class);

    public static String getLink(FishExperiment fishExperiment, boolean suppressPopupLink, boolean suppressMoDetails) {
            return FishPresentation.getLink(fishExperiment.getFish(), suppressPopupLink) + "   "
                    + ExperimentPresentation.getLinkWithChemicalDetails(fishExperiment.getExperiment());



    }

}