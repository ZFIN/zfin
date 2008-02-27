package org.zfin.marker.presentation;

import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.framework.presentation.EntityPresentation;
import org.apache.commons.collections.CollectionUtils;

import java.util.Set;
import java.util.Iterator;

/**
 * Presentation Class to create output from a marker object.
 * This class could be called HTMLMarker opposed to another
 * output format conceivably this be used for.
 */
public class MarkerPresentation extends EntityPresentation {

    public static final String uri = "?MIval=aa-markerview.apg&OID=";

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
     * Generates a link of the relations, with the assumption that the second marker is always the same.
     * XXX[mrt1]gene1,[mrt2]gene2,....[mrtN]geneN
     * XXX is a hit on a cDNA or EST assumed to be a secondMarker
     * e.g.
     * estZ[enc]geneA,[hyb]geneBsecondmarker [rel] gene1
     * if no hit on gene then:
     * estZ[none]
     *
     * @param markerRelationships Set<MarkerRelationship>
     * @return html for marker link
     */
    public static String getRelationLinks(Set<MarkerRelationship> markerRelationships,boolean doAbbrev) {

        if(CollectionUtils.isEmpty(markerRelationships)){
            return null ;
        }

        StringBuffer sb = new StringBuffer() ;

        Marker secondMarker = markerRelationships.iterator().next().getSecondMarker() ;
        sb.append(getWebdriverLink(uri, secondMarker.getZdbID(), getAbbreviation(secondMarker))) ;

     MarkerRelationship markerRelationship = null ;
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
            sb.append(getWebdriverLink(uri, firstMarker.getZdbID(), getAbbreviation(firstMarker))) ;
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
}
