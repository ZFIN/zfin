package org.zfin.mutant.presentation;

import org.apache.log4j.Logger;
import org.zfin.expression.presentation.ExperimentPresentation;
import org.zfin.fish.FishAnnotation;
import org.zfin.fish.GenotypeExperimentFishAnnotation;
import org.zfin.fish.presentation.FishAnnotationPresentation;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.mutant.GenotypeExperiment;

import java.util.Iterator;

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
public class GenotypeExperimentPresentation extends EntityPresentation {

    private static Logger logger = Logger.getLogger(GenotypeExperimentPresentation.class);

    // the last 2 parameters are used only to be passed into ExperimentPresentation.getLink()
    public static String getLink(GenotypeExperiment genotypeExperiment, boolean suppressPopupLink, boolean suppressMoDetails) {
        if (!genotypeExperiment.getGenotypeExperimentFishAnnotations().isEmpty()) {
            Iterator<GenotypeExperimentFishAnnotation> it = genotypeExperiment.getGenotypeExperimentFishAnnotations().iterator();
            StringBuilder sb = new StringBuilder();
            while (it.hasNext()){
                FishAnnotation added = it.next().getFishAnnotation();
                sb.append(FishAnnotationPresentation.getLink(added)).append("   ");
            }
//            logger.error("is not empty");
            return sb.toString();
        } else { // fish is absent
//            logger.error("is empty");
            return GenotypePresentation.getLink(genotypeExperiment.getGenotype(), suppressPopupLink) + "   "
                    + ExperimentPresentation.getLinkWithChemicalDetails(genotypeExperiment.getExperiment());

        }

    }

}