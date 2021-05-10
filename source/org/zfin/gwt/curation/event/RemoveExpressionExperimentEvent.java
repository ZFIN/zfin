package org.zfin.gwt.curation.event;

import com.google.gwt.event.shared.GwtEvent;
import org.zfin.gwt.root.dto.ExpressionExperimentDTO;

public class RemoveExpressionExperimentEvent extends GwtEvent<RemoveExpressionExperimentEventHandler> {
    public static Type<RemoveExpressionExperimentEventHandler> TYPE = new Type<>();

    private ExpressionExperimentDTO experimentDTO;

    public RemoveExpressionExperimentEvent(ExpressionExperimentDTO experimentDTO) {
        this.experimentDTO = experimentDTO;
    }

    @Override
    public Type<RemoveExpressionExperimentEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(RemoveExpressionExperimentEventHandler handler) {
        handler.onEvent(this);
    }

    public ExpressionExperimentDTO getExperimentDTO() {
        return experimentDTO;
    }
}
