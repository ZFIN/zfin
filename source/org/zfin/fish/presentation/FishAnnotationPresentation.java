package org.zfin.fish.presentation;

import org.zfin.fish.FishAnnotation;
import org.zfin.framework.presentation.EntityPresentation;

import org.apache.log4j.Logger;

/**
 *
 * FishAnnotationPresentation is used for fish entites shown in figure view pages, e.g., phenotypeSummary section
 *
 */
public class FishAnnotationPresentation extends EntityPresentation {

    private static final String uri = "/action/fish/fish-detail/";

    private static Logger logger = Logger.getLogger(FishAnnotationPresentation.class);
    /**
     * Generates a Go stage link using the go id
     *
     * @param fishAnnotation Go fish page
     * @return html for marker link
     */
    public static String getLink(FishAnnotation fishAnnotation) {

        if (fishAnnotation.getGenotypeID()!=null){
//            logger.warn("FishAnnotation"+getGeneralHyperLink(uri + fishAnnotation.getFishID(), getName(fishAnnotation)));
            return getGeneralHyperLink(uri + fishAnnotation.getFishID(), getName(fishAnnotation));
        } else{
            return null;
        }
    }

    /**
     * Generate the name of the link
     *
     * @param fishAnnotation fishAnnotation
     * @return name of the hyperlink.
     */
    public static String getName(FishAnnotation fishAnnotation) {
        String stageName = fishAnnotation.getName();
        return getSpanTag("fish", stageName, stageName);
    }

}
