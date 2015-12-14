package org.zfin.gwt.curation.ui;

import com.google.gwt.event.shared.GwtEvent;
import org.zfin.gwt.root.dto.ExpressionFigureStageDTO;

import java.util.List;

public class SelectExpressionEvent extends GwtEvent<SelectExpressionEventHandler> {
    public static Type<SelectExpressionEventHandler> TYPE = new Type<>();
    private List<ExpressionFigureStageDTO> selectedExpressions;

    public SelectExpressionEvent(List<ExpressionFigureStageDTO> selectedExpressions) {
        this.selectedExpressions = selectedExpressions;
    }

    @Override
    public Type<SelectExpressionEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(SelectExpressionEventHandler handler) {
        handler.onEvent(this);
    }

    public List<ExpressionFigureStageDTO> getSelectedExpressions() {
        return selectedExpressions;
    }

}
