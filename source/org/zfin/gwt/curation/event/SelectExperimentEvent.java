package org.zfin.gwt.curation.event;

import com.google.gwt.event.shared.GwtEvent;
import org.zfin.gwt.root.dto.ExperimentDTO;

public class SelectExperimentEvent extends GwtEvent<SelectExperimentEventHandler> {
    public static Type<SelectExperimentEventHandler> TYPE = new Type<>();

    private ExperimentDTO experimentDTO;

    public SelectExperimentEvent(ExperimentDTO experimentDTO) {
        this.experimentDTO = experimentDTO;
    }

    @Override
    public Type<SelectExperimentEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(SelectExperimentEventHandler handler) {
        handler.onEvent(this);
    }

    public ExperimentDTO getExperimentDTO() {
        return experimentDTO;
    }
}
