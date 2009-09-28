package org.zfin.marker.presentation.event;

public class SupplierChangeEvent {

    private String supplierName ;

    public SupplierChangeEvent(String supplierName){
        this.supplierName = supplierName;
    }

    public String getSupplierName() {
        return supplierName;
    }
}
