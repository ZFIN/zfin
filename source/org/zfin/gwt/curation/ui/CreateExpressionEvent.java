package org.zfin.gwt.curation.ui;

import com.google.gwt.event.shared.GwtEvent;
import org.zfin.gwt.root.dto.ExpressionFigureStageDTO;

import java.util.List;

public class CreateExpressionEvent extends GwtEvent<CreateExpressionEventHandler> {
    public static Type<CreateExpressionEventHandler> TYPE = new Type<>();

    public CreateExpressionEvent(List<ExpressionFigureStageDTO> figureStageDTOList) {
        this.figureStageDTOList = figureStageDTOList;
    }

    private List<ExpressionFigureStageDTO> figureStageDTOList;
    @Override
    public Type<CreateExpressionEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CreateExpressionEventHandler handler) {
        handler.onEvent(this);
    }

    public List<ExpressionFigureStageDTO> getFigureStageDTOList() {
        return figureStageDTOList;
    }
}
