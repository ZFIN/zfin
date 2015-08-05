package org.zfin.mutant.presentation;

import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.marker.Marker;
import org.zfin.mutant.Genotype;

/**
 * Presentation Class to create output from a Genotype object.
 * This class could be called HTMLMarker opposed to another
 * output format conceivably this be used for.
 */
public class GenotypePresentation extends EntityPresentation {

    private static final String uri = "genotype/view/";
    private static final String popupUri = "genotype/genotype-detail-popup?zdbID=";

    /**
     * Generates an html formatted Genotype name
     *
     * @param genotype Genotype
     * @return html for Genotype link
     */
    public static String getName(Genotype genotype) {
        return getHtmlTag(getDisplayName(genotype));
    }

    public static String getHtmlTag(String displayName) {
        String cssClassName = Marker.TypeGroup.GENEDOM.toString().toLowerCase();
        return getSpanTag(cssClassName, displayName, displayName);

    }

    public static String getDisplayName(Genotype genotype) {
        return genotype.getAbbreviation() + getBackground(genotype);
    }

    /**
     * Generates a Genotype link using the Abbreviation
     *
     * @param genotype Genotype
     * @return html for marker link
     */
    public static String getLink(Genotype genotype) {
        if (getBackground(genotype) != null) {
            return getTomcatLink(uri, genotype.getZdbID(), getHtmlTag(getDisplayName(genotype)), null);
        }
        return getTomcatLink(uri, genotype.getZdbID(), getHtmlTag(getDisplayName(genotype)), null);
    }

    public static String getLink(Genotype genotype, boolean suppressPopupLink) {
        StringBuilder sb = new StringBuilder();
        if (genotype.isWildtype())
            sb.append(getTomcatLink(uri, genotype.getZdbID(), getHtmlTag(genotype.getHandle()), null));
        else
            sb.append(getTomcatLink(uri, genotype.getZdbID(), getHtmlTag(getDisplayName(genotype)), null));
        if (!suppressPopupLink)
            sb.append(getPopupLink(genotype));

        return sb.toString();
    }

    public static String getBackground(Genotype genotype) {
        return genotype.getBackgroundDisplayName();
    }

    public static String getPopupLink(Genotype genotype) {
        return getTomcatPopupLink(popupUri, String.valueOf(genotype.getZdbID()),
                "More details about this genotype");
    }

}
