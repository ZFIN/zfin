package org.zfin.gwt.curation.event;

import com.google.gwt.event.shared.GwtEvent;
import org.zfin.gwt.root.dto.ExpressionExperimentDTO;

public class UpdateExpressionExperimentEvent extends GwtEvent<UpdateExpressionExperimentEventHandler> {
    public static Type<UpdateExpressionExperimentEventHandler> TYPE = new Type<>();

    private ExpressionExperimentDTO experimentDTO;

    public UpdateExpressionExperimentEvent(ExpressionExperimentDTO experimentDTO) {
        this.experimentDTO = experimentDTO;
    }

    @Override
    public Type<UpdateExpressionExperimentEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(UpdateExpressionExperimentEventHandler handler) {
        handler.onEvent(this);
    }

    public ExpressionExperimentDTO getExperimentDTO() {
        return experimentDTO;
    }
}
