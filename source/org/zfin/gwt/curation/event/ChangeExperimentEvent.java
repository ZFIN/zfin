package org.zfin.gwt.curation.event;

import com.google.gwt.event.shared.GwtEvent;
import org.zfin.gwt.root.dto.EnvironmentDTO;
import org.zfin.gwt.root.dto.FeatureDTO;

import java.util.List;

public class ChangeExperimentEvent extends GwtEvent<ChangeExperimentEventHandler> {
    public static Type<ChangeExperimentEventHandler> TYPE = new Type<>();




    @Override
    public Type<ChangeExperimentEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ChangeExperimentEventHandler handler) {
        handler.onAdd(this);
        handler.onUpdate(this);
        handler.onDelete(this);
    }




}
