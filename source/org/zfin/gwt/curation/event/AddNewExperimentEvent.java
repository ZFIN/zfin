package org.zfin.gwt.curation.event;

import com.google.gwt.event.shared.GwtEvent;
import org.zfin.gwt.root.dto.EnvironmentDTO;
import org.zfin.gwt.root.dto.FeatureDTO;

import java.util.List;

public class AddNewExperimentEvent extends GwtEvent<AddNewExperimentEventHandler> {
    public static Type<AddNewExperimentEventHandler> TYPE = new Type<>();




    @Override
    public Type<AddNewExperimentEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(AddNewExperimentEventHandler handler) {
        handler.onAdd(this);
    }




}
