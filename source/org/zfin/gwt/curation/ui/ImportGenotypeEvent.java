package org.zfin.gwt.curation.ui;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by cmpich on 6/19/15.
 */
public class ImportGenotypeEvent extends GwtEvent<ImportGenotypeEventHandler> {
    public static Type<ImportGenotypeEventHandler> TYPE = new Type<>();

    @Override
    public Type<ImportGenotypeEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ImportGenotypeEventHandler handler) {
        handler.onImportGenotype(this);
    }
}
