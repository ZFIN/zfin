package org.zfin.gwt.curation.event;

import com.google.gwt.event.shared.GwtEvent;

public class DirtyValueEvent extends GwtEvent<DirtyValueEventHandler> {
    public static Type<DirtyValueEventHandler> TYPE = new Type<>();

    private Boolean dirty;

    public Boolean getDirty() {
        return dirty;
    }

    public void setDirty(Boolean dirty) {
        this.dirty = dirty;
    }

    @Override
    public Type<DirtyValueEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(DirtyValueEventHandler handler) {
        handler.onDirtyEvent(this);
    }
}
