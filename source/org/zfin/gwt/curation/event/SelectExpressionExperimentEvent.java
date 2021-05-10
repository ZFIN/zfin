package org.zfin.gwt.curation.event;

import com.google.gwt.event.shared.GwtEvent;
import org.zfin.gwt.root.dto.ExpressionExperimentDTO;

public class SelectExpressionExperimentEvent extends GwtEvent<SelectExpressionExperimentEventHandler> {
    public static Type<SelectExpressionExperimentEventHandler> TYPE = new Type<>();

    private boolean ckecked;
    private ExpressionExperimentDTO experimentDTO;

    public SelectExpressionExperimentEvent(boolean ckecked, ExpressionExperimentDTO experimentDTO) {
        this.ckecked = ckecked;
        this.experimentDTO = experimentDTO;
    }

    @Override
    public Type<SelectExpressionExperimentEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(SelectExpressionExperimentEventHandler handler) {
        handler.onEvent(this);
    }

    public boolean isCkecked() {
        return ckecked;
    }

    public ExpressionExperimentDTO getExperimentDTO() {
        return experimentDTO;
    }
}
