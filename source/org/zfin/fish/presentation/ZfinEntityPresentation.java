package org.zfin.fish.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.zfin.feature.Feature;
import org.zfin.feature.presentation.FeaturePresentation;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.infrastructure.ActiveData;
import org.zfin.infrastructure.ZfinEntity;
import org.zfin.marker.*;
import org.zfin.marker.presentation.MarkerPresentation;
import org.zfin.marker.presentation.RelatedMarker;
import org.zfin.ontology.presentation.TermPresentation;
import org.zfin.properties.ZfinProperties;
import org.zfin.publication.presentation.FigurePresentation;
import org.zfin.publication.presentation.PublicationPresentation;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Presentation Class to create output from a marker object.
 * This class could be called HTMLMarker opposed to another
 * output format conceivably this be used for.
 */
public class ZfinEntityPresentation extends EntityPresentation {

    private static final Logger logger = Logger.getLogger(ZfinEntityPresentation.class);

    /**
     * Generates an html formatted marker name
     *
     * @param marker ZfinEntity
     * @return html for marker link
     */
    // ToDo: needs to be implemented like the getLink() method.
    public static String getName(ZfinEntity marker) {
        return getSpanTag(null, marker.getName(), marker.getID());
    }

    /**
     * Generates a Marker link using the Abbreviation, splits based on marker type because
     * instanceof doesn't reliably know when it's a more specific class
     *
     * @param entity Marker
     * @return html for marker link
     */
    public static String getLink(ZfinEntity entity) {
        if (ActiveData.validateID(entity.getID()) == ActiveData.Type.FIG)
            return FigurePresentation.getLinkByZfinEntity(entity);
        if (ActiveData.validateID(entity.getID()) == ActiveData.Type.TERM)
            return TermPresentation.getLinkByZfinEntity(entity);
        return MarkerPresentation.getMarkerLinkByZfinEntity(entity);
    }

    /**
     * Generates an html formatted marker abbreviation/symbol
     *
     * @param marker Marker
     * @return html for marker abbrev / symbol
     */
    public static String getAbbreviation(Marker marker) {
        String cssClassName;
        if (marker.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
            cssClassName = Marker.TypeGroup.GENEDOM.toString().toLowerCase();
        } else if (marker.isInTypeGroup(Marker.TypeGroup.CONSTRUCT)) {
            cssClassName = Marker.TypeGroup.CONSTRUCT.toString().toLowerCase();
        } else {
            cssClassName = NONGENEDOMMARKER;
        }
        return getSpanTag(cssClassName, marker.getName(), marker.getAbbreviation());
    }

}
