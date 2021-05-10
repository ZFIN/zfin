package org.zfin.marker.presentation;

import org.apache.commons.lang.StringUtils;
import org.zfin.marker.Marker;

/**
 *
 */
public class MergeBean extends DeleteBean {

    private String markerToMergeIntoViewString;
    private Marker markerToMergeInto;


    public String getMarkerToMergeIntoViewString() {
        if (StringUtils.isEmpty(markerToMergeIntoViewString) && markerToMergeInto != null) {
            markerToMergeIntoViewString = markerToMergeInto.getAbbreviation();
        }
        return markerToMergeIntoViewString;
    }

    public void setMarkerToMergeIntoViewString(String markerToMergeIntoViewString) {
        this.markerToMergeIntoViewString = markerToMergeIntoViewString;
    }

    public Marker getMarkerToMergeInto() {
        return markerToMergeInto;
    }

    public void setMarkerToMergeInto(Marker markerToMergeInto) {
        this.markerToMergeInto = markerToMergeInto;
    }
}
