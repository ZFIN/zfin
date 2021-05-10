package org.zfin.fish.presentation;

import org.zfin.fish.FishAnnotation;
import org.zfin.framework.presentation.EntityPresentation;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.zfin.marker.Marker;

/**
 *
 * FishAnnotationPresentation is used for fish entites shown in figure view pages, e.g., phenotypeSummary section
 *
 */
public class FishAnnotationPresentation extends EntityPresentation {

    private static final String uri = "/action/fish/fish-detail/";
    private static final String popupUri = "fish/fish-detail-popup/";

    private static Logger logger = LogManager.getLogger(FishAnnotationPresentation.class);
    /**
     * Generates a Go stage link using the go id
     *
     * @param fishAnnotation Go fish page
     * @return html for marker link
     */
    public static String getLink(FishAnnotation fishAnnotation) {

        if (fishAnnotation.getGenotypeID()!=null){
            return getGeneralHyperLink(uri + fishAnnotation.getFishID(), getName(fishAnnotation)) +
                    getTomcatPopupLink(popupUri, fishAnnotation.getFishID(), "More details about this fish");
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
        String name = fishAnnotation.getName();
        String cssClassName = Marker.TypeGroup.GENEDOM.toString().toLowerCase();
        return getSpanTag(cssClassName, name, name);
    }

}
