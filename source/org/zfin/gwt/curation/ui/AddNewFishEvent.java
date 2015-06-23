package org.zfin.gwt.curation.ui;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by cmpich on 6/19/15.
 */
public class AddNewFishEvent extends GwtEvent<AddNewFishEventHandler> {
    public static Type<AddNewFishEventHandler> TYPE = new Type<>();

    @Override
    public Type<AddNewFishEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(AddNewFishEventHandler handler) {
        handler.onAddFish(this);
    }
}
