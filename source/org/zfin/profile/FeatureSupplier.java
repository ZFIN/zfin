package org.zfin.profile;

import org.apache.commons.lang.ObjectUtils;
import org.zfin.feature.Feature;

import java.io.Serializable;

/**
 * Main domain object for lab and company info
 */
public class FeatureSupplier extends ObjectSupplier implements Serializable, Comparable<FeatureSupplier> {

    private Feature feature;

    public int hashCode() {
        int num = 39;
        if (feature != null) {
            num += feature.hashCode();
        }
        if (feature != null) {
            num += feature.hashCode();
        }
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
        if (o == null) {
            return false;
        }
        if (!(o instanceof FeatureSupplier supplier)) {
            return false;
        }

        if (feature == null) {
            throw new RuntimeException("Feature is null but should not!");
        }
        if (organization == null) {
            throw new RuntimeException("organization is null but should not!");
        }

        return feature.equals(supplier.getFeature()) &&
                ObjectUtils.equals(organization, supplier.getOrganization());
    }

    public int compareTo(FeatureSupplier anotherSupplier) {
        if (anotherSupplier == null || anotherSupplier.getOrganization() == null || anotherSupplier.getOrganization().getName() == null) {
            return -1;
        }
        if (getOrganization() == null || getOrganization().getName() == null) {
            return +1;
        }
        return getOrganization().getName().compareToIgnoreCase(anotherSupplier.getOrganization().getName());
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
        dataZdbID = feature.getZdbID();
    }

    public boolean isZirc() {
        return isOrg("ZDB-LAB-991005-53");
    }

    public boolean isEzrc() {
        return isOrg("ZDB-LAB-130607-1");
    }

    public boolean isCzrc() {
        return isOrg("ZDB-LAB-130226-1");
    }

    public boolean isMoensLab() {
        return isOrg("ZDB-LAB-990225-6");
    }

    public boolean isSolnicaLab() {
        return isOrg("ZDB-LAB-981208-2");
    }

    public boolean isRiken() {
        return isOrg("ZDB-LAB-070718-1");
    }

    private boolean isOrg(String labID) {
        return getOrganization().getZdbID().equals(labID);
    }
}
