package org.zfin.mutant.presentation;

import org.apache.log4j.Logger;
import org.zfin.expression.presentation.ExperimentPresentation;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.mutant.FishExperiment;

/**
 *
 * This class is used to populate elements under MartFish column in figure view's tables
 * Previously, when fish was absent, the experiment would still show up, and we wanted to avoid that
 *
 * Notice that in expressionTable.tag, <zfin:link entity="${row.genotypeExperiment}"/> was originally
 * <zfin:link entity="${row.genotype}"/> &nbsp;
 * <zfin:link entity="${row.experiment}"/>
 *
 */
public class GenotypeExperimentPresentation extends EntityPresentation {

    private static Logger logger = Logger.getLogger(GenotypeExperimentPresentation.class);

    // the last 2 parameters are used only to be passed into ExperimentPresentation.getLink()
    public static String getLink(FishExperiment fishExperiment, boolean suppressPopupLink, boolean suppressMoDetails) {
//            logger.error("is empty");
        // TODO: needs to be a fish insteads of genotype
            return GenotypePresentation.getLink(fishExperiment.getFish().getGenotype(), suppressPopupLink) + "   "
                    + ExperimentPresentation.getLinkWithChemicalDetails(fishExperiment.getExperiment());



    }

}