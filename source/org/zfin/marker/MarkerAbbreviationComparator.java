package org.zfin.marker;

import java.util.Comparator;

/**
 */
public class MarkerAbbreviationComparator implements  Comparator<Marker>{

    private String compareString  ;

    public MarkerAbbreviationComparator(String compareString){
        this.compareString = compareString ;
    }

    /**
     * Compare against startsWith name first
     * Compare against abbreviation seconds
     * @param marker
     * @param marker1
     * @return
     */
    @Override
    public int compare(Marker marker, Marker marker1) {
        if(marker.getAbbreviation().startsWith(compareString)
                &&
                false==marker1.getAbbreviation().startsWith(compareString)
                ){
            return -1 ;
        }
        else
        if(false==marker.getAbbreviation().startsWith(compareString)
                &&
                marker1.getAbbreviation().startsWith(compareString)
                ){
            return 1 ;
        }
        // if neither one starts with the string, just return based on abbreviation order
        else{
            return marker.compareTo(marker1);
        }
    }
}
