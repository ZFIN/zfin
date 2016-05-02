package org.zfin.gwt.curation.ui;

import com.google.gwt.event.shared.GwtEvent;

public class CreateExpressionEvent extends GwtEvent<CreateExpressionEventHandler> {
    public static Type<CreateExpressionEventHandler> TYPE = new Type<>();

    @Override
    public Type<CreateExpressionEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CreateExpressionEventHandler handler) {
        handler.onEvent(this);
    }
}
