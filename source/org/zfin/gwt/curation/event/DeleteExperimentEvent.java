package org.zfin.gwt.curation.event;

import com.google.gwt.event.shared.GwtEvent;

public class DeleteExperimentEvent extends GwtEvent<DeleteExperimentEventHandler> {
    public static Type<DeleteExperimentEventHandler> TYPE = new Type<>();




    @Override
    public Type<DeleteExperimentEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(DeleteExperimentEventHandler handler) {
        handler.onDelete(this);
    }




}
