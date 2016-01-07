package org.zfin.gwt.curation.event;

import com.google.gwt.event.shared.GwtEvent;
import org.zfin.gwt.root.dto.ExperimentDTO;

public class AddExpressionExperimentEvent extends GwtEvent<AddExpressionExperimentEventHandler> {
    public static Type<AddExpressionExperimentEventHandler> TYPE = new Type<>();

    @Override
    public Type<AddExpressionExperimentEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(AddExpressionExperimentEventHandler handler) {
        handler.onEvent(this);
    }
}
