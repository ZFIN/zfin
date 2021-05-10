package org.zfin.gwt.curation.event;

import com.google.gwt.event.shared.GwtEvent;

public class AddAttributeEvent extends GwtEvent<AddAttributeEventHandler> {
    public static Type<AddAttributeEventHandler> TYPE = new Type<>();

    @Override
    public Type<AddAttributeEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(AddAttributeEventHandler handler) {
        handler.onEvent(this);
    }
}
