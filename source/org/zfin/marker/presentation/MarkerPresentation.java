package org.zfin.marker.presentation;

import org.zfin.marker.Marker;
import org.zfin.marker.presentation.RelatedMarker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.publication.presentation.PublicationPresentation;
import org.zfin.properties.ZfinProperties;
import org.apache.commons.collections.CollectionUtils;

import java.util.Set;
import java.util.Iterator;

/**
 * Presentation Class to create output from a marker object.
 * This class could be called HTMLMarker opposed to another
 * output format conceivably this be used for.
 */
public class MarkerPresentation extends EntityPresentation {

    public static final String marker_uri = "?MIval=aa-markerview.apg&OID=";
    public static final String transcript_uri = "marker/transcript-view?zdbID=";
    public static final String clone_uri = "marker/clone-view?zdbID=";
    public static final String antibody_uri = "antibody/detail?antibody.zdbID=";

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
     * Generates a Marker link using the Abbreviation, splits based on marker type because
     * isntanceof doesn't reliably know when it's a more specific class
     *
     * @param marker Marker
     * @return html for marker link
     */
    public static String getLink(Marker marker) {
        if (marker.isInTypeGroup(Marker.TypeGroup.TRANSCRIPT)) {
            return getTranscriptLink(marker);
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
     * [atp6va0a1|http://zfin.org/cgi-bin/webdriver?MIval=aa-markerview.apg&OID=ZDB-GENE-030131-302|ATPase, H+ transporting, lysosomal V0 subunit a isoform 1]
     * @param marker Marker to render.
     * @return A rendered wiki link.
     */
    public static String getWikiLink(Marker marker){
        return getWikiLink(ZfinProperties.getWebDriver()+marker_uri,marker.getZdbID(),"_"+marker.getAbbreviation()+"_",marker.getName()) ;
    }


    public static String getMarkerLink(Marker marker) {
        return getWebdriverLink(marker_uri, marker.getZdbID(), getAbbreviation(marker));
    }

    public static String getTranscriptLink(Marker marker) {
        return getTomcatLink(transcript_uri, marker.getZdbID(), getName(marker), null);
    }

    public static String getCloneLink(Marker marker) {
//        return getTomcatLink(clone_uri, marker.getZdbID(), getAbbreviation(marker));
        return getWebdriverLink(marker_uri, marker.getZdbID(), getAbbreviation(marker));
    }

    public static String getAntibodyLink(Marker marker) {
        return getTomcatLink(antibody_uri, marker.getZdbID(), getAbbreviation(marker), null);
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
     * @param doAbbrev Add an abbreviation.
 * @return html for marker link
     */
    public static String getRelationLinks(Set<MarkerRelationship> markerRelationships,boolean doAbbrev) {

        if(CollectionUtils.isEmpty(markerRelationships)){
            return null ;
        }

        StringBuffer sb = new StringBuffer() ;

        Marker secondMarker = markerRelationships.iterator().next().getSecondMarker() ;
        sb.append(getLink(secondMarker)) ;

     MarkerRelationship markerRelationship ;
        for(
                Iterator<MarkerRelationship> iter = markerRelationships.iterator() ;
                iter.hasNext() ;
        ){
            markerRelationship = iter.next() ; 
            Marker firstMarker = markerRelationship.getFirstMarker() ;
            MarkerRelationship.Type type = markerRelationship.getType() ;
            sb.append("[") ;

            if(doAbbrev){
                if(type.equals(MarkerRelationship.Type.GENE_CONTAINS_SMALL_SEGMENT)){
                    sb.append("CO")  ;
                }
                else
                if(type.equals(MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT)){
                    sb.append("EN")  ;
                }
                else
                if(type.equals(MarkerRelationship.Type.GENE_HYBRIDIZED_BY_SMALL_SEGMENT)){
                    sb.append("HY")  ;
                }
                else{
                    sb.append(type)  ;
                }
            }
            else{
                sb.append(type)  ;
            }
            sb.append("]") ; 
            sb.append(getLink(firstMarker)) ;
            if(iter.hasNext()){
                sb.append(",") ; 
            }
        }

        return sb.toString() ;
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

    /**
     * Create an attribution link for a MarkerDBLink
     * @param relatedMarker to attribute, ok if it has no attributions
     * @return link html
     */
    public static String getAttributionLink(RelatedMarker relatedMarker) {

        MarkerRelationship mrel = relatedMarker.getMarkerRelationship();

        StringBuilder sb = new StringBuilder("");

        if (mrel.getPublicationCount() == 1) {
            sb.append(" (");
            sb.append(PublicationPresentation.getLink(mrel.getSinglePublication(), "1"));
            sb.append(")");
        } else if (mrel.getPublicationCount() > 1) {
            /* todo: there should be some more infrastructure for the showpubs links */
            StringBuilder uri = new StringBuilder("?MIval=aa-showpubs.apg");
            uri.append("&orgOID=");
            uri.append(relatedMarker.getMarker().getZdbID());
            uri.append("&rtype=marker&recattrsrctype=standard");
            uri.append("&OID=");
            String count = String.valueOf(mrel.getPublicationCount());

            sb.append(" (");
            sb.append(getWebdriverLink(uri.toString(), mrel.getZdbID(), count));
            sb.append(")");
        }

        return sb.toString();
    }



}
