package org.zfin.profile.presentation;

import org.zfin.profile.Address;
import org.zfin.profile.service.BeanFieldUpdate;

import java.io.Serializable;
import java.util.List;

/**
 */
public class UpdateAddressMessageBean implements Serializable{

    private List<BeanFieldUpdate> fields;
    private String securityPersonZdbID;
    private Address address ;

    public List<BeanFieldUpdate> getFields() {
        return fields;
    }

    public void setFields(List<BeanFieldUpdate> fields) {
        this.fields = fields;
    }

    public String getSecurityPersonZdbID() {
        return securityPersonZdbID;
    }

    public void setSecurityPersonZdbID(String securityPersonZdbID) {
        this.securityPersonZdbID = securityPersonZdbID;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
