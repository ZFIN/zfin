package org.zfin.marker.presentation;

import org.zfin.sequence.MarkerDBLink;

import java.util.Comparator;

/**
 */
public class DbLinkMarkerSortComparator implements Comparator<MarkerDBLink>{

    @Override
    public int compare(MarkerDBLink dbLink1, MarkerDBLink dbLink2) {

        int comparator ;

        // compare type first, then abbreviation
        comparator = dbLink1.getMarker().getMarkerType().getName().compareTo(dbLink2.getMarker().getMarkerType().getName());
        if (comparator != 0)  return comparator ;

//        comparator = dbLink1.getMarker().getAbbreviation().compareTo(dbLink2.getMarker().getAbbreviation());
//        if (comparator != 0)  return comparator ;

        comparator = dbLink1.getMarker().getAbbreviationOrder().compareTo(dbLink2.getMarker().getAbbreviationOrder());
        if (comparator != 0)  return comparator ;

        comparator = dbLink1.getReferenceDatabase().getForeignDBDataType().getDisplayOrder()
                - dbLink2.getReferenceDatabase().getForeignDBDataType().getDisplayOrder();
        if (comparator != 0)  return comparator ;

        comparator = dbLink1.getReferenceDatabase().getForeignDB().getSignificance()
                - dbLink2.getReferenceDatabase().getForeignDB().getSignificance();
        if (comparator != 0)  return comparator ;
//
        if(dbLink2.getLength()!=null && dbLink1.getLength()!=null){
            comparator = dbLink2.getLength()
                    - dbLink1.getLength();
        }
        if(dbLink1==null && dbLink2 !=null){
            return 1 ;
        }
        if(dbLink1!=null && dbLink2 ==null){
            return -1 ;
        }
        if (comparator != 0)  return comparator ;

        // compare query, really?
        // not doing this,

        return  dbLink2.getAccessionNumberDisplay().compareTo(dbLink1.getAccessionNumberDisplay());
    }
}
