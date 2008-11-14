package org.zfin.people;

import org.apache.commons.lang.ObjectUtils;
import org.zfin.marker.Marker;

import java.io.Serializable;

/**
 * Main domain object for lab and company info
 */
public class MarkerSupplier extends ObjectSupplier implements Serializable, Comparable<MarkerSupplier> {

    private Marker marker;

    public String getOrderURL() {
        if (organization.getOrganizationOrderURL() != null && organization.getOrganizationOrderURL().getUrlPrefix() != null && accNum != null)
          return organization.getOrganizationOrderURL().getUrlPrefix() + accNum;
        return null;
    }

    public int hashCode() {
        int num = 39;
        if (marker != null)
            num += marker.hashCode();
        if (marker != null)
            num += marker.hashCode();
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
        MarkerSupplier supplier = (MarkerSupplier) o;

        if (marker == null)
            throw new RuntimeException("Marker is null but should not!");
        if (organization == null)
            throw new RuntimeException("organization is null but should not!");

        return marker.equals(supplier.getMarker()) &&
                ObjectUtils.equals(organization, supplier.getOrganization());
    }

    public int compareTo(MarkerSupplier anotherSupplier) {
        if (anotherSupplier == null || anotherSupplier.getOrganization() == null || anotherSupplier.getOrganization().getName() == null)
            return -1;
        if (getOrganization() == null || getOrganization().getName() == null)
            return +1;
        return getOrganization().getName().compareToIgnoreCase(anotherSupplier.getOrganization().getName());
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
        dataZdbID = marker.getZdbID();
    }
}
