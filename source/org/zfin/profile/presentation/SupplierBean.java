package org.zfin.profile.presentation;

import org.zfin.profile.Organization;

public class SupplierBean {

    String zdbID;
    String name;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static SupplierBean convert(Organization supplier) {
        SupplierBean newBean = new SupplierBean();
        newBean.setZdbID(supplier.getZdbID());
        newBean.setName(supplier.getName());
        return newBean;
    }

}
