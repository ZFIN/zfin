package org.zfin.gwt.curation.event;

import com.google.gwt.event.shared.GwtEvent;

public class CurationEvent extends GwtEvent<TabEventHandler> {
    public static Type<TabEventHandler> TYPE = new Type<>();

    public EventType eventType;
    private String description;

    public CurationEvent(EventType eventType) {
        this.eventType = eventType;
    }

    public CurationEvent(EventType eventType, String description) {
        this.description = description;
        this.eventType = eventType;
    }

    @Override
    public Type<TabEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(TabEventHandler handler) {
        handler.onEvent(this);
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getDescription() {
        return description;
    }
}
