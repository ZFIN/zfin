package org.zfin.marker.presentation.client;

import org.zfin.marker.presentation.event.SupplierChangeEvent;

public interface CanRemoveSupplier {
    public void fireSupplierRemoved(SupplierChangeEvent supplierChangeEvent) ;
}
