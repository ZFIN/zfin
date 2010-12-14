package org.zfin.people;

import org.apache.commons.lang.ObjectUtils;
import org.zfin.feature.Feature;

import java.io.Serializable;

/**
 */
public class FeatureSource extends ObjectSource implements Serializable, Comparable<FeatureSource> {

    private Feature feature;


    public int hashCode() {
        int num = 39;
        if (feature != null)
            num += feature.hashCode();
        if (feature != null)
            num += feature.hashCode();
        return num;
    }

    /**
     * This method assumes that dataZdbID and supplierZdbID are not null.
     * Otherwise this method throws an exception.
     *
     * @param o Object
     * @return boolean
     */
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof MarkerSupplier))
            return false;
        FeatureSource source = (FeatureSource) o;

        if (feature == null)
            throw new RuntimeException("Marker is null but should not!");
        if (organization == null)
            throw new RuntimeException("organization is null but should not!");

        return feature.equals(source.getFeature()) &&
                ObjectUtils.equals(organization, source.getOrganization());
    }

    public int compareTo(FeatureSource anotherSource) {
        if (anotherSource == null || anotherSource.getOrganization() == null || anotherSource.getOrganization().getName() == null)
            return -1;
        if (getOrganization() == null || getOrganization().getName() == null)
            return +1;
        return getOrganization().getName().compareToIgnoreCase(anotherSource.getOrganization().getName());
    }


    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }
}
