package org.zfin.marker.presentation;

import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.marker.Marker;
import org.zfin.mutant.Genotype;

/**
 * Presentation Class to create output from a Genotype object.
 * This class could be called HTMLMarker opposed to another
 * output format conceivably this be used for.
 */
public class GenotypePresentation extends EntityPresentation {

    private static final String uri = "genotype/detail?genotype.zdbID=";
    /**
     * Generates an html formatted Genotype name
     *
     * @return html for Genotype link
     * @param genotype Genotype
     */
    public static String getName(Genotype genotype) {
        String cssClassName = Marker.TypeGroup.GENEDOM.toString();
        return getSpanTag(cssClassName, genotype.getName(), genotype.getName());
    }

    /**
     * Generates a Genotype link using the Abbreviation
     *
     * @param genotype Genotype
     * @return html for marker link
     */
    public static String getLink(Genotype genotype) {
        return getTomcatLink(uri, genotype.getZdbID(), genotype.getName(), null);
    }

}
