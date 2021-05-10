package org.zfin.feature.presentation;

import org.zfin.feature.Feature;
import org.zfin.feature.FeaturePrefix;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.marker.Marker;
import org.zfin.mutant.repository.FeaturePresentationBean;

/**
 * Presentation Class to create output from a Feature object.
 * This class could be called HTMLMarker opposed to another
 * output format conceivably this be used for.
 */
public class FeaturePrefixPresentation extends EntityPresentation {

    private static final String uri = "feature/alleles/";
    /**
     * Generates an html formatted Genotype name
     *
     * @return html for Feature link
     * @param prefix FeaturePrefix
     */
    public static String getName(FeaturePrefix prefix) {
        return getSpanTag("", prefix.getEntityName(), prefix.getEntityName());
    }

    /**
     * Generates a Feature link using the Abbreviation
     *
     * @param prefix FeaturePrefix
     * @return html for feature link
     */
    public static String getLink(FeaturePrefix prefix) {
        return getTomcatLink(uri, prefix.getZdbID(), prefix.getEntityName(), null);
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