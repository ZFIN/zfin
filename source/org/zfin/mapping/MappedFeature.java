package org.zfin.mapping;

import org.zfin.feature.Feature;

public class MappedFeature extends MappedMarker {

    private Feature feature;

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public int compareTo(Object o) {
        if (o == null) {
            return -1;
        } else if (!(o instanceof MappedMarkerImpl)) {
            return o.toString().compareTo(toString());
        }
        // both MappedMarker
        else {
            MappedFeature mappedMarker = (MappedFeature) o;
            if (!lg.equalsIgnoreCase(mappedMarker.getLg())) {
                return lg.toLowerCase().compareTo(mappedMarker.getLg().toLowerCase());
            } else {
                return feature.compareTo(mappedMarker.getFeature());
            }
        }
    }

    @Override
    public String getEntityID() {
        return feature.getZdbID();
    }

    @Override
    public String getEntityAbbreviation() {
        return feature.getAbbreviation();
    }
}
