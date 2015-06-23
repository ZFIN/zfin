package org.zfin.gwt.curation.ui;

import com.google.gwt.event.shared.GwtEvent;

public class AddNewGenotypeEvent extends GwtEvent<AddNewGenotypeEventHandler> {
    public static Type<AddNewGenotypeEventHandler> TYPE = new Type<>();

    @Override
    public Type<AddNewGenotypeEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(AddNewGenotypeEventHandler handler) {
        handler.onAddGenotype(this);
    }
}
