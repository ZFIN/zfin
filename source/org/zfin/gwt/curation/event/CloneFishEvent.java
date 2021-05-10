package org.zfin.gwt.curation.event;

import com.google.gwt.event.shared.GwtEvent;
import org.zfin.gwt.root.dto.FishDTO;

public class CloneFishEvent extends GwtEvent<CloneFishEventHandler> {
    public static Type<CloneFishEventHandler> TYPE = new Type<>();

    private FishDTO fish;

    @Override
    public Type<CloneFishEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CloneFishEventHandler handler) {
        handler.onClone(this);
    }

    public FishDTO getFish() {
        return fish;
    }

    public void setFish(FishDTO fish) {
        this.fish = fish;
    }
}
