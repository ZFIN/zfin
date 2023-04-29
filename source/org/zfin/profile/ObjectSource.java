package org.zfin.profile;

import org.apache.commons.lang.ObjectUtils;

import java.io.Serializable;

/**
 */
public class ObjectSource implements Serializable {
    protected long id;
    protected String dataZdbID;
    protected Organization organization;


    public String getDataZdbID() {
        return dataZdbID;
    }

    public void setDataZdbID(String dataZdbID) {
        this.dataZdbID = dataZdbID;
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
        if (!(o instanceof ObjectSource source))
            return false;

        if (organization == null)
            throw new RuntimeException("Organization is null but should not!");
        if (dataZdbID == null)
            throw new RuntimeException("dataZdbID is null but should not!");

        return dataZdbID.equals(source.getDataZdbID()) &&
                ObjectUtils.equals(organization, source.getOrganization());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
