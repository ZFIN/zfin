package org.zfin.gwt.curation.event;

import com.google.gwt.event.shared.GwtEvent;

public class ChangeConditionEvent extends GwtEvent<ChangeConditionEventHandler> {
    public static Type<ChangeConditionEventHandler> TYPE = new Type<>();


    @Override
    public Type<ChangeConditionEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ChangeConditionEventHandler handler) {
        handler.onChange(this);
    }


}
