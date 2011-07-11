package org.zfin.marker.service;

import org.hibernate.transform.ResultTransformer;
import org.zfin.marker.presentation.MarkerRelationshipPresentation;
import org.zfin.marker.presentation.OrganizationLink;

import java.util.*;

/**
 */
public class MarkerRelationshipPresenationTransformer implements ResultTransformer {

    private boolean is1to2 = false;

    public MarkerRelationshipPresenationTransformer(boolean is1to2) {
        this.is1to2 = is1to2;
    }


    @Override
    public Object transformTuple(Object[] tuple, String[] aliases) {
        MarkerRelationshipPresentation returnObject = new MarkerRelationshipPresentation();
        returnObject.setIs1To2(is1to2);
        returnObject.setAbbreviation(tuple[0].toString());
        returnObject.setZdbId(tuple[1].toString());
        returnObject.setAbbreviationOrder(tuple[2].toString());
        returnObject.setMarkerType(tuple[3].toString());
        returnObject.setRelationshipType(tuple[4].toString());
        returnObject.setLink(tuple[5].toString());
        if (tuple[6] != null) {
            returnObject.setAttributionZdbID(tuple[6].toString());
        }
        if (tuple.length>7 && tuple[7] !=null) {
            OrganizationLink organizationLink = new OrganizationLink();

            if (tuple[7] != null) {
                organizationLink.setSupplierZdbId(tuple[7].toString());
            }
            if (tuple[8] != null) {
                organizationLink.setAccessionNumber(tuple[8].toString());
            }
            if (tuple[9] != null) {
                organizationLink.setSourceUrl(tuple[9].toString());
            }
            if (tuple[10] != null) {
                organizationLink.setUrlDisplayText(tuple[10].toString());
            }
            returnObject.addOrganizationLink(organizationLink);
        }

        return returnObject;
    }

    @Override
    public List transformList(List collection) {

        Map<String, MarkerRelationshipPresentation> map = new HashMap<String, MarkerRelationshipPresentation>();
        // compact by supplier first
        for (Object o : collection) {
            MarkerRelationshipPresentation mrp = (MarkerRelationshipPresentation) o;
            MarkerRelationshipPresentation mrpStored = map.get(mrp.getZdbId());
            if (mrpStored!=null) {
                mrpStored.addAttributionLink(mrp.getAttributionZdbID());
                mrpStored.addOrganizationLinks(mrp.getOrganizationLinks());
                map.put(mrpStored.getZdbId(), mrpStored);

            } else {
                map.put(mrp.getZdbId(), mrp);
            }
        }


        List<MarkerRelationshipPresentation> relationships = new ArrayList<MarkerRelationshipPresentation>();
        relationships.addAll(map.values());

        Collections.sort(relationships, new Comparator<MarkerRelationshipPresentation>() {
            @Override
            public int compare(MarkerRelationshipPresentation mr1, MarkerRelationshipPresentation mr2) {
                int compare = mr1.getRelationshipType().compareTo(mr2.getRelationshipType());
                if (compare != 0) return compare;

                return mr1.getAbbreviationOrder().compareTo(mr2.getAbbreviationOrder());
            }
        });

        return relationships;
    }
}
