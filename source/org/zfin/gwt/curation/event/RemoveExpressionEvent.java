package org.zfin.gwt.curation.event;

import com.google.gwt.event.shared.GwtEvent;
import org.zfin.gwt.root.dto.ExpressionExperimentDTO;

public class RemoveExpressionEvent extends GwtEvent<RemoveExpressionEventHandler> {
    public static Type<RemoveExpressionEventHandler> TYPE = new Type<>();

    private ExpressionExperimentDTO experimentDTO;

    public RemoveExpressionEvent(ExpressionExperimentDTO experimentDTO) {
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

    public ExpressionExperimentDTO getExperimentDTO() {
        return experimentDTO;
    }
}
