package org.zfin.profile;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.ObjectUtils;
import org.zfin.framework.api.View;

import java.io.Serializable;

/**
 * Main domain object for lab and company info
 */
@Setter
@Getter
public abstract class ObjectSupplier implements Serializable {

    protected String dataZdbID;
    @JsonView(View.API.class)
    protected Organization organization;

    protected String accNum;
    protected String availState;

    @JsonView(View.API.class)
    public String getOrderURL() {
        if (organization.getOrganizationOrderURL() != null && organization.getOrganizationOrderURL().getUrlPrefix() != null && accNum != null)
            return organization.getOrganizationOrderURL().getUrlPrefix() + accNum;
        return null;
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