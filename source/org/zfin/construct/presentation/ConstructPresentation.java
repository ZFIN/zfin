package org.zfin.construct.presentation;

import org.zfin.construct.ConstructCuration;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.marker.Marker;

/**
 * Presentation Class to create output from a Feature object.
 * This class could be called HTMLMarker opposed to another
 * output format conceivably this be used for.
 */
public class ConstructPresentation extends EntityPresentation {

    /**
     * Generates an html formatted Genotype name
     *
     * @return html for Feature link
     * @param construct ConstructCuration
     */
    public static String getName(ConstructCuration construct) {
        String cssClassName = Marker.TypeGroup.GENEDOM.toString();
        return getSpanTag(cssClassName, construct.getName(), construct.getName());
    }

    /**
     * Generates a Feature link using the Abbreviation
     *
     * @param construct
     * @return html for feature link
     */
    public static String getLink(ConstructCuration construct) {
        return getViewLink(construct.getZdbID(), construct.getName(), construct.getName());
    }

}