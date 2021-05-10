package org.zfin.marker.service;

import org.hibernate.transform.ResultTransformer;
import org.zfin.marker.presentation.MarkerRelationshipPresentation;
import org.zfin.marker.presentation.OrganizationLink;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

/**
 */
public class MarkerRelationshipSupplierPresentationTransformer implements ResultTransformer {

    private boolean is1to2 = false;

    public MarkerRelationshipSupplierPresentationTransformer(boolean is1to2) {
        this.is1to2 = is1to2;
    }

    private static MarkerRepository mr = RepositoryFactory.getMarkerRepository();
    @Override
    public Object transformTuple(Object[] tuple, String[] aliases) {

        MarkerRelationshipPresentation returnObject = new MarkerRelationshipPresentation();
        returnObject.setIs1To2(is1to2);
        returnObject.setAbbreviation(tuple[0].toString());
        returnObject.setZdbId(tuple[1].toString());
        returnObject.setZdbID(tuple[1].toString());
        returnObject.setAbbreviationOrder(tuple[2].toString());
        returnObject.setMarkerType(tuple[3].toString());
        returnObject.setRelationshipType(tuple[4].toString());
        if(tuple[3].toString().equalsIgnoreCase("Gene")) {
            returnObject.setLink("<i>" + tuple[5].toString() + "</i>");
        }
        else {
            returnObject.setLink(tuple[5].toString());
        }

        if (tuple[6] != null) {
            returnObject.addAttributionZdbID(tuple[6].toString());
        }
        if (tuple.length>7 && tuple[7] !=null) {
            OrganizationLink organizationLink = new OrganizationLink();

            if (tuple[7] != null) {
                organizationLink.setSupplierZdbId(tuple[7].toString());
            }
            if (tuple.length>8 && tuple[8] != null) {
                organizationLink.setAccessionNumber(tuple[8].toString());
            }
            if (tuple.length>9 && tuple[9] != null) {
                organizationLink.setSourceUrl(tuple[9].toString());
            }
            if (tuple.length>10 && tuple[10] != null) {
                organizationLink.setUrlDisplayText(tuple[10].toString());
            }
            returnObject.addOrganizationLink(organizationLink);
        }
        if(tuple.length>11 && tuple[11]!=null){
            returnObject.setMarkerRelationshipZdbId(tuple[11].toString());
        }

        return returnObject;
    }

    @Override
    public List transformList(List collection) {

        Map<Integer, MarkerRelationshipPresentation> map = new HashMap<>();
        // compact by supplier first
        for (Object o : collection) {
            MarkerRelationshipPresentation mrp = (MarkerRelationshipPresentation) o;
            MarkerRelationshipPresentation mrpStored = map.get(mrp.hashCode());
            if (mrpStored!=null) {
                mrpStored.addAttributionZdbID(mrp.getAttributionZdbID());
                mrpStored.addOrganizationLinks(mrp.getOrganizationLinks());
                map.put(mrp.hashCode(), mrpStored);

            } else {
                map.put(mrp.hashCode(), mrp);
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
