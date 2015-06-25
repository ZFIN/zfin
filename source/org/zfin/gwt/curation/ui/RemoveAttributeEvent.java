package org.zfin.gwt.curation.ui;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by cmpich on 6/19/15.
 */
public class RemoveAttributeEvent extends GwtEvent<RemoveAttributeEventHandler> {
    public static Type<RemoveAttributeEventHandler> TYPE = new Type<>();

    @Override
    public Type<RemoveAttributeEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(RemoveAttributeEventHandler handler) {
        handler.onRemoveAttribute(this);
    }
}
