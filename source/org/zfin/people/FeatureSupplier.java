package org.zfin.people;

import org.apache.commons.lang.ObjectUtils;
import org.zfin.mutant.Feature;

import java.io.Serializable;

/**
 * Main domain object for lab and company info
 */
public class FeatureSupplier extends ObjectSupplier implements Serializable, Comparable<FeatureSupplier> {

    private Feature feature;

    public String getOrderURL() {
        if (organization.getOrganizationOrderURL() != null && organization.getOrganizationOrderURL().getUrlPrefix() != null && accNum != null)
          return organization.getOrganizationOrderURL().getUrlPrefix() + accNum;
        return null;
    }

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
        if (!(o instanceof FeatureSupplier))
            return false;
        FeatureSupplier supplier = (FeatureSupplier) o;

        if (feature == null)
            throw new RuntimeException("Feature is null but should not!");
        if (organization == null)
            throw new RuntimeException("organization is null but should not!");

        return feature.equals(supplier.getFeature()) &&
                ObjectUtils.equals(organization, supplier.getOrganization());
    }

    public int compareTo(FeatureSupplier anotherSupplier) {
        if (anotherSupplier == null || anotherSupplier.getOrganization() == null || anotherSupplier.getOrganization().getName() == null)
            return -1;
        if (getOrganization() == null || getOrganization().getName() == null)
            return +1;
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
		if (this.getOrganization().getZdbID().equals("ZDB-LAB-991005-53"))
		  return true;
		else
		  return false;
	}

	public boolean isMoensLab() {
		if (this.getOrganization().getZdbID().equals("ZDB-LAB-990225-6"))
		  return true;
		else
		  return false;
	}

	public boolean isRiken() {
		if (this.getOrganization().getZdbID().equals("ZDB-LAB-070718-1"))
		  return true;
		else
		  return false;
	}
}
