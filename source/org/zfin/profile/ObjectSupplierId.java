package org.zfin.profile;

import java.io.Serializable;
import java.util.Objects;

public class ObjectSupplierId implements Serializable {
    private String dataZdbID;
    private String organization;

    public ObjectSupplierId() {
    }

    public ObjectSupplierId(String dataZdbID, String organization) {
        this.dataZdbID = dataZdbID;
        this.organization = organization;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ObjectSupplierId that = (ObjectSupplierId) o;
        return Objects.equals(dataZdbID, that.dataZdbID) &&
                Objects.equals(organization, that.organization);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataZdbID, organization);
    }
}
