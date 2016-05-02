package org.zfin.gwt.curation.event;

import com.google.gwt.event.shared.GwtEvent;
import org.zfin.gwt.root.dto.FeatureDTO;

public class AddNewFeatureEvent extends GwtEvent<AddNewFeatureEventHandler> {
    public static Type<AddNewFeatureEventHandler> TYPE = new Type<>();

    private FeatureDTO feature;

    public AddNewFeatureEvent(FeatureDTO feature) {
        this.feature = feature;
    }

    @Override
    public Type<AddNewFeatureEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(AddNewFeatureEventHandler handler) {
        handler.onAdd(this);
    }

    public FeatureDTO getFeature() {
        return feature;
    }
}
