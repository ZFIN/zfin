package org.zfin.people;

import org.apache.commons.lang.ObjectUtils;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Prita
 * Date: Jul 23, 2009
 * Time: 12:02:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class ObjectSource implements Serializable {

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
        if (!(o instanceof ObjectSource))
            return false;
        ObjectSource source = (ObjectSource) o;

        if (organization == null)
            throw new RuntimeException("Organization is null but should not!");
        if (dataZdbID == null)
            throw new RuntimeException("dataZdbID is null but should not!");

        return dataZdbID.equals(source.getDataZdbID()) &&
                ObjectUtils.equals(organization, source.getOrganization());
    }

}
