package org.zfin.gwt.curation.event;

import com.google.gwt.event.shared.GwtEvent;
import org.zfin.gwt.root.dto.ExpressionExperimentDTO;

import java.util.Map;

public class AddExpressionExperimentEvent extends GwtEvent<AddExpressionExperimentEventHandler> {
    public static Type<AddExpressionExperimentEventHandler> TYPE = new Type<>();

    private Map<ExpressionExperimentDTO, Integer> expressionExperimentDTOMap;

    public AddExpressionExperimentEvent(Map<ExpressionExperimentDTO, Integer> expressionExperimentDTOMap) {
        this.expressionExperimentDTOMap = expressionExperimentDTOMap;
    }

    public Map<ExpressionExperimentDTO, Integer> getExpressionExperimentDTOMap() {
        return expressionExperimentDTOMap;
    }

    @Override
    public Type<AddExpressionExperimentEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(AddExpressionExperimentEventHandler handler) {
        handler.onEvent(this);
    }
}
