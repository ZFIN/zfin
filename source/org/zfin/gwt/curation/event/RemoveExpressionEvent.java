package org.zfin.gwt.curation.event;

import com.google.gwt.event.shared.GwtEvent;
import org.zfin.gwt.root.dto.ExperimentDTO;

public class RemoveExpressionEvent extends GwtEvent<RemoveExpressionEventHandler> {
    public static Type<RemoveExpressionEventHandler> TYPE = new Type<>();

    private ExperimentDTO experimentDTO;

    public RemoveExpressionEvent(ExperimentDTO experimentDTO) {
        this.experimentDTO = experimentDTO;
    }

    @Override
    public Type<RemoveExpressionEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(RemoveExpressionEventHandler handler) {
        handler.onEvent(this);
    }

    public ExperimentDTO getExperimentDTO() {
        return experimentDTO;
    }
}
