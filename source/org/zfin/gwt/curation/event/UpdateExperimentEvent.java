package org.zfin.gwt.curation.event;

import com.google.gwt.event.shared.GwtEvent;

public class UpdateExperimentEvent extends GwtEvent<UpdateExperimentEventHandler> {
    public static Type<UpdateExperimentEventHandler> TYPE = new Type<>();




    @Override
    public Type<UpdateExperimentEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(UpdateExperimentEventHandler handler) {
        handler.onUpdate(this);
    }




}
