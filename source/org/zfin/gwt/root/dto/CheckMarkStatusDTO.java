package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.List;

/**
 * Holds values for the check mark status.
 */
public class CheckMarkStatusDTO implements IsSerializable {

    private ExpressionFigureStageDTO figureAnnotation;
    private List<ExpressionFigureStageDTO> figureAnnotations;

    public ExpressionFigureStageDTO getFigureAnnotation() {
        return figureAnnotation;
    }

    public void setFigureAnnotation(ExpressionFigureStageDTO figureAnnotation) {
        this.figureAnnotation = figureAnnotation;
    }

    public List<ExpressionFigureStageDTO> getFigureAnnotations() {
        return figureAnnotations;
    }

    public void setFigureAnnotations(List<ExpressionFigureStageDTO> figureAnnotations) {
        this.figureAnnotations = figureAnnotations;
    }
}