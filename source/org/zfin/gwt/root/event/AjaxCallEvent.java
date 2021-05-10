package org.zfin.gwt.root.event;

import com.google.gwt.event.shared.GwtEvent;
import org.zfin.gwt.root.ui.ZfinModule;

public class AjaxCallEvent extends GwtEvent<AjaxCallEventHandler> {
    public static Type<AjaxCallEventHandler> TYPE = new Type<>();

    public AjaxCallEventType eventType;
    ZfinModule module;

    public AjaxCallEvent(AjaxCallEventType eventType) {
        this.eventType = eventType;
    }

    public AjaxCallEvent(AjaxCallEventType eventType, ZfinModule module) {
        this.eventType = eventType;
        this.module = module;
    }

    @Override
    public Type<AjaxCallEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(AjaxCallEventHandler handler) {
        handler.onAjaxCall(this);
    }

    public AjaxCallEventType getEventType() {
        return eventType;
    }

    public ZfinModule getModule() {
        return module;
    }
}
