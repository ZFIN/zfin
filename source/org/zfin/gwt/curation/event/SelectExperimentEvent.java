package org.zfin.gwt.curation.event;

import com.google.gwt.event.shared.GwtEvent;
import org.zfin.gwt.root.dto.ExpressionExperimentDTO;

public class SelectExperimentEvent extends GwtEvent<SelectExperimentEventHandler> {
    public static Type<SelectExperimentEventHandler> TYPE = new Type<>();

    private ExpressionExperimentDTO experimentDTO;

    public SelectExperimentEvent(ExpressionExperimentDTO experimentDTO) {
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

    public ExpressionExperimentDTO getExperimentDTO() {
        return experimentDTO;
    }
}
