package org.zfin.mutant.presentation;

import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.marker.Marker;
import org.zfin.mutant.Genotype;
import org.zfin.ontology.Term;

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
        String cssClassName = Marker.TypeGroup.GENEDOM.toString().toLowerCase();
        return getSpanTag(cssClassName, getDisplayName(genotype), getDisplayName(genotype));
    }

    public static String getDisplayName(Genotype genotype) {
        return genotype.getName() + getBackground(genotype);
    }

    /**
     * Generates a Genotype link using the Abbreviation
     *
     * @param genotype Genotype
     * @return html for marker link
     */
    public static String getLink(Genotype genotype) {
        if (getBackground(genotype) != null) {
            return getTomcatLink(uri, genotype.getZdbID(), getDisplayName(genotype), null);
        }
        return getTomcatLink(uri, genotype.getZdbID(), getDisplayName(genotype), null);
    }

    public static String getLink(Genotype genotype, boolean suppressPopupLink) {
        StringBuilder sb = new StringBuilder();
        if (getBackground(genotype) != null)
            sb.append(getTomcatLink(uri, genotype.getZdbID(), getDisplayName(genotype), null));
        else if (!genotype.isWildtype())
            sb.append(getTomcatLink(uri, genotype.getZdbID(), getName(genotype), null));
        else {
            sb.append(getTomcatLink(uri, genotype.getZdbID(), genotype.getHandle(), null));
        }
//        if (genotype.isWildtype()) suppressPopupLink = true;
        if (!suppressPopupLink)
            sb.append(getPopupLink(genotype));

        return sb.toString();
    }

    public static String getBackground(Genotype genotype) {
        if (genotype.getBackground() != null) {
            String cssClassName = Marker.TypeGroup.GENEDOM.toString().toLowerCase();
            String backgroundAppendix = " (";
            backgroundAppendix += genotype.getBackground().getAbbreviation();
            backgroundAppendix += ")";
            return getSpanTag(cssClassName, backgroundAppendix, backgroundAppendix);
        }
        return "";
    }

    public static String getPopupLink(Genotype genotype) {
        StringBuilder sb = new StringBuilder(100);
        sb.append(getTomcatPopupLink(popupUri, String.valueOf(genotype.getZdbID()),
                "More details about this genotype"));
        return sb.toString();
    }

}
