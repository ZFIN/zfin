package org.zfin.people;

import org.apache.commons.lang.ObjectUtils;

import java.io.Serializable;

/**
 * Main domain object for lab and company info
 */
public abstract class ObjectSupplier implements Serializable {

    protected String dataZdbID;
    protected Organization organization;

    protected String accNum;
    protected String availState;

    public String getOrderURL() {
        if (organization.getOrganizationOrderURL() != null && organization.getOrganizationOrderURL().getUrlPrefix() != null && accNum != null)
            return organization.getOrganizationOrderURL().getUrlPrefix() + accNum;
        return null;
    }

    public String getDataZdbID() {
        return dataZdbID;
    }

    public void setDataZdbID(String dataZdbID) {
        this.dataZdbID = dataZdbID;
    }

    public String getAccNum() {
        return accNum;
    }

    public void setAccNum(String accNum) {
        this.accNum = accNum;
    }

    public String getAvailState() {
        return availState;
    }

    public void setAvailState(String availState) {
        this.availState = availState;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public int hashCode() {
        int num = 39;
        if (organization != null)
            num += organization.hashCode();
        if (dataZdbID != null)
            num += dataZdbID.hashCode();
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
        if (!(o instanceof ObjectSupplier))
            return false;
        ObjectSupplier supplier = (ObjectSupplier) o;

        if (organization == null)
            throw new RuntimeException("Organization is null but should not!");
        if (dataZdbID == null)
            throw new RuntimeException("dataZdbID is null but should not!");

        return dataZdbID.equals(supplier.getDataZdbID()) &&
                ObjectUtils.equals(organization, supplier.getOrganization());
    }

}