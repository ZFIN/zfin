package org.zfin.gwt.marker.event;

public class SupplierChangeEvent {

    private String supplierName ;

    public SupplierChangeEvent(String supplierName){
        this.supplierName = supplierName;
    }

    public String getSupplierName() {
        return supplierName;
    }
}
