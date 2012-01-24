package org.zfin.marker.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.zfin.feature.Feature;
import org.zfin.feature.presentation.FeaturePresentation;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.infrastructure.ActiveData;
import org.zfin.infrastructure.ZfinEntity;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.MarkerType;
import org.zfin.marker.Transcript;
import org.zfin.properties.ZfinProperties;
import org.zfin.publication.presentation.PublicationPresentation;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Presentation Class to create output from a marker object.
 * This class could be called HTMLMarker opposed to another
 * output format conceivably this be used for.
 */
public class MarkerPresentation extends EntityPresentation {

    private static final Logger logger = Logger.getLogger(MarkerPresentation.class);
    public static final String marker_uri = "marker/view/";

    /**
     * Generates an html formatted marker name
     *
     * @param marker Marker
     * @return html for marker link
     */
    public static String getName(Marker marker) {
        String cssClassName;
        if (marker.isInTypeGroup(Marker.TypeGroup.GENEDOM) || marker.isInTypeGroup(Marker.TypeGroup.EFG)){
            cssClassName = Marker.TypeGroup.GENEDOM.toString().toLowerCase();
        }
        else
        if (marker.isInTypeGroup(Marker.TypeGroup.CONSTRUCT)){
            cssClassName = Marker.TypeGroup.CONSTRUCT.toString().toLowerCase();
        }
        else{
            cssClassName = NONGENEDOMMARKER;
        }
        return getSpanTag(cssClassName, marker.getAbbreviation(), marker.getName());
    }

    /**
     * Generates a Marker link using the Abbreviation, splits based on marker type because
     * instanceof doesn't reliably know when it's a more specific class
     *
     * @param marker Marker
     * @return html for marker link
     */
    public static String getLink(Marker marker) {
        if (marker.isInTypeGroup(Marker.TypeGroup.TRANSCRIPT)) {
            try {
                return getTranscriptLink((Transcript) marker);
            } catch (ClassCastException e) {
                return getTranscriptLink(RepositoryFactory.getMarkerRepository().getTranscriptByZdbID(marker.getZdbID()));
            }
        } else if (marker.isInTypeGroup(Marker.TypeGroup.ATB)) {
            return getAntibodyLink(marker);
        } else if (marker.isInTypeGroup(Marker.TypeGroup.CLONE) || marker.isInTypeGroup(Marker.TypeGroup.SMALLSEG)) {
            return getCloneLink(marker);
        } else {
            return getMarkerLink(marker);
        }

    }

    /**
     * Should be of the form.
     * [atp6va0a1|http://zfin.org/action/marker/view/ZDB-GENE-030131-302|ATPase, H+ transporting, lysosomal V0 subunit a isoform 1]
     *
     * @param marker Marker to render.
     * @return A rendered wiki link.
     */
    public static String getWikiLink(Marker marker) {
        return getWikiLink(ZfinProperties.getWebDriver() + marker_uri, marker.getZdbID(), "_" + marker.getAbbreviation() + "_", marker.getName());
    }


    public static String getMarkerLink(Marker marker) {
        return getTomcatLink(marker_uri, marker.getZdbID(), getAbbreviation(marker), marker.getName());
    }

    public static String getMarkerLinkByZfinEntity(ZfinEntity entity) {
        String ID = entity.getID();
        ActiveData.Type dataType = ActiveData.validateID(ID);
        switch (dataType) {
            case GENE:
                Marker marker = getMarkerFromEntity(entity, Marker.Type.GENE, Marker.TypeGroup.GENEDOM);
                return MarkerPresentation.getMarkerLink(marker);
            case ALT:
                Feature feature = new Feature();
                feature.setAbbreviation(entity.getName());
                feature.setName(entity.getName());
                feature.setZdbID(entity.getID());
                return FeaturePresentation.getLink(feature);
            case MRPHLNO:
                marker = getMarkerFromEntity(entity, Marker.Type.MRPHLNO, Marker.TypeGroup.MRPHLNO);
                return MarkerPresentation.getMarkerLink(marker);
            case TGCONSTRCT:
                marker = getMarkerFromEntity(entity, Marker.Type.TGCONSTRCT, Marker.TypeGroup.CONSTRUCT);
                return MarkerPresentation.getMarkerLink(marker);
            case ETCONSTRCT:
                marker = getMarkerFromEntity(entity, Marker.Type.ETCONSTRCT, Marker.TypeGroup.CONSTRUCT);
                return MarkerPresentation.getMarkerLink(marker);
            case GTCONSTRCT:
                marker = getMarkerFromEntity(entity, Marker.Type.GTCONSTRCT, Marker.TypeGroup.CONSTRUCT);
                return MarkerPresentation.getMarkerLink(marker);
            case EFG:
                marker = getMarkerFromEntity(entity, Marker.Type.EFG, Marker.TypeGroup.EFG);
                return MarkerPresentation.getMarkerLink(marker);
            case PTCONSTRCT:
                marker = getMarkerFromEntity(entity, Marker.Type.PTCONSTRCT, Marker.TypeGroup.CONSTRUCT);
                return MarkerPresentation.getMarkerLink(marker);
            case SSLP:
                marker = getMarkerFromEntity(entity, Marker.Type.SSLP, Marker.TypeGroup.SSLP);
                return MarkerPresentation.getMarkerLink(marker);
            case EST:
                marker = getMarkerFromEntity(entity, Marker.Type.EST, Marker.TypeGroup.EST);
                return MarkerPresentation.getMarkerLink(marker);
            case PAC:
                marker = getMarkerFromEntity(entity, Marker.Type.EST, Marker.TypeGroup.PAC);
                return MarkerPresentation.getMarkerLink(marker);
            case STS:
                marker = getMarkerFromEntity(entity, Marker.Type.EST, Marker.TypeGroup.STS);
                return MarkerPresentation.getMarkerLink(marker);
            case RAPD:
                marker = getMarkerFromEntity(entity, Marker.Type.EST, Marker.TypeGroup.RAPD);
                return MarkerPresentation.getMarkerLink(marker);
            default:
                marker = getMarkerFromEntity(entity, Marker.Type.GENE, Marker.TypeGroup.GENEDOM);
        }
        return null;
    }

    private static Marker getMarkerFromEntity(ZfinEntity entity, Marker.Type type, Marker.TypeGroup groupType ) {
        Marker marker = new Marker();
        marker.setAbbreviation(entity.getName());
        marker.setName(entity.getName());
        marker.setZdbID(entity.getID());
        MarkerType markerType = new MarkerType();
        markerType.setType(type);
        Set<Marker.TypeGroup> groups = new HashSet<Marker.TypeGroup>(1);
        groups.add(groupType);
        markerType.setTypeGroups(groups);
        marker.setMarkerType(markerType);
        return marker;
    }

    public static String getTranscriptLink(Transcript transcript) {
        return getTomcatLink(marker_uri, transcript.getZdbID(), getName(transcript), null)  + (transcript.isWithdrawn() ? WITHDRAWN: "") ;
    }

    public static String getCloneLink(Marker marker) {
//        return getTomcatLink(clone_uri, marker.getZdbID(), getAbbreviation(marker));
        return getTomcatLink(marker_uri, marker.getZdbID(), getAbbreviation(marker));
    }

    public static String getAntibodyLink(Marker marker) {
        return getTomcatLink(marker_uri, marker.getZdbID(), marker.getName(), null,marker.getAbbreviation());
    }

    /**
     * Generates a link of the relations, with the assumption that the second marker is always the same.
     * XXX[mrt1]gene1,[mrt2]gene2,....[mrtN]geneN
     * XXX is a hit on a cDNA or EST assumed to be a secondMarker
     * e.g.
     * estZ[enc]geneA,[hyb]geneBsecondmarker [rel] gene1
     * if no hit on gene then:
     * estZ[none]
     *
     * @param markerRelationships Set<MarkerRelationship>
     * @param doAbbrev            Add an abbreviation.
     * @return html for marker link
     */
    public static String getRelationLinks(Set<MarkerRelationship> markerRelationships, boolean doAbbrev) {

        if (CollectionUtils.isEmpty(markerRelationships)) {
            return null;
        }

        StringBuffer sb = new StringBuffer();

        Marker secondMarker = markerRelationships.iterator().next().getSecondMarker();
        sb.append(getLink(secondMarker));

        MarkerRelationship markerRelationship;
        for (
                Iterator<MarkerRelationship> iter = markerRelationships.iterator();
                iter.hasNext();
                ) {
            markerRelationship = iter.next();
            Marker firstMarker = markerRelationship.getFirstMarker();
            MarkerRelationship.Type type = markerRelationship.getType();
            sb.append("[");

            if (doAbbrev) {
                if (type.equals(MarkerRelationship.Type.GENE_CONTAINS_SMALL_SEGMENT)) {
                    sb.append("CO");
                } else if (type.equals(MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT)) {
                    sb.append("EN");
                } else if (type.equals(MarkerRelationship.Type.GENE_HYBRIDIZED_BY_SMALL_SEGMENT)) {
                    sb.append("HY");
                } else {
                    sb.append(type);
                }
            } else {
                sb.append(type);
            }
            sb.append("]");
            sb.append(getLink(firstMarker));
            if (iter.hasNext()) {
                sb.append(",");
            }
        }

        return sb.toString();
    }

    /**
     * Generates an html formatted marker abbreviation/symbol
     *
     * @param marker Marker
     * @return html for marker abbrev / symbol
     */
    public static String getAbbreviation(Marker marker) {
        String cssClassName;
        if (marker.isInTypeGroup(Marker.TypeGroup.GENEDOM)){
            cssClassName = Marker.TypeGroup.GENEDOM.toString().toLowerCase();
        }
        else
        if (marker.isInTypeGroup(Marker.TypeGroup.CONSTRUCT)){
            cssClassName = Marker.TypeGroup.CONSTRUCT.toString().toLowerCase();
        }
        else{
            cssClassName = NONGENEDOMMARKER;
        }
        return getSpanTag(cssClassName, marker.getName(), marker.getAbbreviation());
    }

    /**
     * Create an attribution link for a MarkerDBLink
     *
     * @param marker to attribute, ok if it has no attributions
     * @return link html
     */
    public static String getAttributionLink(Marker marker) {

        if (marker == null)
            return null;

        return getAttributionLink(marker.getZdbID(), marker.getZdbID(),
                marker.getPublications().iterator().next().getSourceZdbID(), marker.getPublications().size());

    }

    /**
     * Create an attribution link for a MarkerDBLink
     *
     * @param relatedMarker to attribute, ok if it has no attributions
     * @return link html
     */
    public static String getAttributionLink(RelatedMarker relatedMarker) {

        MarkerRelationship mrel = relatedMarker.getMarkerRelationship();


        return getAttributionLink(relatedMarker.getMarker().getZdbID(),
                                  mrel.getZdbID(),
                mrel.getSinglePublication().getZdbID(),
                mrel.getPublicationCount());

    }


    public static String getAttributionLink(PreviousNameLight previousName) {
        if (previousName == null)
            return null;

        return getAttributionLink(previousName.getMarkerZdbID(),
                previousName.getAliasZdbID(),
                previousName.getPublicationZdbID(),
                previousName.getPublicationCount());

    }


    public static String getAttributionLink(String markerZdbID, String additionalZdbID,
                                            String publicationZdbID, int publicationCount) {
        if (publicationCount == 0)
            return null;
        if (publicationCount == 1) {
            return PublicationPresentation.getSingleAttributionLink(publicationZdbID, publicationCount);
        } else {
            return PublicationPresentation.getMultipleAttributionLink(markerZdbID, additionalZdbID,
                    "marker", "standard", publicationCount);
        }
    }

}
