package org.zfin.marker.presentation;

import org.zfin.marker.Marker;
import org.zfin.framework.presentation.EntityPresentation;

/**
 * Presentation Class to create output from a marker object.
 * This class could be called HTMLMarker opposed to another
 * output format conceivably this be used for.
 */
public class MarkerPresentation extends EntityPresentation {

    private static final String uri = "?MIval=aa-markerview.apg&OID=";

    /**
     * Generates an html formatted marker name
     *
     * @return html for marker link
     * @param marker Marker
     */
    public static String getName(Marker marker) {
        String cssClassName;
        if (marker.isInTypeGroup(Marker.TypeGroup.GENEDOM))
            cssClassName = Marker.TypeGroup.GENEDOM.toString().toLowerCase();
        else if (marker.isInTypeGroup(Marker.TypeGroup.CONSTRUCT))
            cssClassName = Marker.TypeGroup.CONSTRUCT.toString().toLowerCase();
        else
            cssClassName = NONGENEDOMMARKER;
        return getSpanTag(cssClassName, marker.getAbbreviation(), marker.getName());
    }

    /**
     * Generates a Marker link using the Abbreviation
     *
     * @param marker Marker
     * @return html for marker link
     */
    public static String getLink(Marker marker) {
        return getWebdriverLink(uri, marker.getZdbID(), getAbbreviation(marker));
    }

    /**
     * Generates an html formatted marker abbreviation/symbol
     *
     * @param marker Marker
     * @return html for marker abbrev / symbol
     */
    public static String getAbbreviation(Marker marker) {
        String cssClassName;
        if (marker.isInTypeGroup(Marker.TypeGroup.GENEDOM))
            cssClassName = Marker.TypeGroup.GENEDOM.toString().toLowerCase();
        else
            cssClassName = NONGENEDOMMARKER;
        return getSpanTag(cssClassName, marker.getName(), marker.getAbbreviation());
    }
}
