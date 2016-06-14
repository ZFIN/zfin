package org.zfin.gwt.curation.event;

import com.google.gwt.event.shared.GwtEvent;
import org.zfin.gwt.root.dto.FeatureDTO;

public class AddNewExperimentEvent extends GwtEvent<AddNewExperimentEventHandler> {
    public static Type<AddNewExperimentEventHandler> TYPE = new Type<>();

    private FeatureDTO feature;

    public AddNewExperimentEvent(FeatureDTO feature) {
        this.feature = feature;
    }

    @Override
    public Type<AddNewExperimentEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(AddNewExperimentEventHandler handler) {
        handler.onAdd(this);
    }

    public FeatureDTO getFeature() {
        return feature;
    }
}
