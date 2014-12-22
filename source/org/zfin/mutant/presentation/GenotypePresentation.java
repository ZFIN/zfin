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
     * @return html for Genotype link
     * @param genotype Genotype
     */
    public static String getName(Genotype genotype) {
        String cssClassName = Marker.TypeGroup.GENEDOM.toString().toLowerCase();
        return getSpanTag(cssClassName, genotype.getName(), genotype.getName());
    }

    /**
     * Generates a Genotype link using the Abbreviation
     *
     * @param genotype Genotype
     * @return html for marker link
     */
    public static String getLink(Genotype genotype) {
        if (getBackground(genotype) != null) {
            return getTomcatLink(uri, genotype.getZdbID(), getName(genotype)+"("+getBackground(genotype)+")", null);
        }
        return getTomcatLink(uri, genotype.getZdbID(), getName(genotype), null);
    }

    public static String getLink(Genotype genotype, boolean suppressPopupLink) {
        StringBuilder sb = new StringBuilder();
        if (getBackground(genotype) != null)
            sb.append(getTomcatLink(uri, genotype.getZdbID(), getName(genotype)+"("+getBackground(genotype)+")", null));
        else
            sb.append(getTomcatLink(uri, genotype.getZdbID(), getName(genotype), null));
//        if (genotype.isWildtype()) suppressPopupLink = true;
        if (!suppressPopupLink)
            sb.append(getPopupLink(genotype));

        return sb.toString();
    }

    public static String getBackground(Genotype genotype) {
        if (!genotype.getAssociatedGenotypes().isEmpty()) {
            String cssClassName = Marker.TypeGroup.GENEDOM.toString().toLowerCase();
            String backgroundNames = new String("");
            for (Genotype bkgrd : genotype.getAssociatedGenotypes()) {
                 backgroundNames = backgroundNames + bkgrd.getName();
            }
            return getSpanTag(cssClassName, backgroundNames, backgroundNames);
        }

        return null;
    }

    public static String getPopupLink(Genotype genotype) {
        StringBuilder sb = new StringBuilder(100);
        sb.append(getTomcatPopupLink(popupUri, String.valueOf(genotype.getZdbID()),
                "More details about this genotype"));
        return sb.toString();
    }

}
