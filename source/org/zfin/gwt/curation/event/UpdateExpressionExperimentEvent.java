package org.zfin.gwt.curation.event;

import com.google.gwt.event.shared.GwtEvent;
import org.zfin.gwt.root.dto.ExperimentDTO;

public class UpdateExpressionExperimentEvent extends GwtEvent<UpdateExpressionExperimentEventHandler> {
    public static Type<UpdateExpressionExperimentEventHandler> TYPE = new Type<>();

    private ExperimentDTO experimentDTO;

    public UpdateExpressionExperimentEvent(ExperimentDTO experimentDTO) {
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

    public ExperimentDTO getExperimentDTO() {
        return experimentDTO;
    }
}
