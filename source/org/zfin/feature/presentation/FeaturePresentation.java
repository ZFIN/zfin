package org.zfin.feature.presentation;

import org.zfin.feature.Feature;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.marker.Marker;
import org.zfin.mutant.repository.FeaturePresentationBean;

/**
 * Presentation Class to create output from a Feature object.
 * This class could be called HTMLMarker opposed to another
 * output format conceivably this be used for.
 */
public class FeaturePresentation extends EntityPresentation {

    private static final String uri = "feature/feature-detail?zdbID=";
    /**
     * Generates an html formatted Genotype name
     *
     * @return html for Feature link
     * @param feature Feature
     */
    public static String getName(Feature feature) {
        String cssClassName = Marker.TypeGroup.GENEDOM.toString();
        return getSpanTag(cssClassName, feature.getName(), feature.getName());
    }

    /**
     * Generates a Feature link using the Abbreviation
     *
     * @param feature Feature
     * @return html for feature link
     */
    public static String getLink(Feature feature) {
        return getTomcatLink(uri, feature.getZdbID(), feature.getName(), null);
    }

    /**
     * Generates a Feature link using the Abbreviation
     *
     * @param feature Feature
     * @return html for feature link
     */
    public static String getLink(FeaturePresentationBean feature) {
        return getTomcatLink(uri, feature.getFeatureZdbId(), feature.getAbbrevation(), null);
    }

}