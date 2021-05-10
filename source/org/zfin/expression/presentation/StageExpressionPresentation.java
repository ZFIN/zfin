package org.zfin.expression.presentation;

import org.zfin.anatomy.DevelopmentStage;

/**
 */
public class StageExpressionPresentation{

    private DevelopmentStage startStage;
    private DevelopmentStage endStage;

    public DevelopmentStage getStartStage() {
        return startStage;
    }

    public DevelopmentStage getEndStage() {
        return endStage;
    }

    public void setStartStage(DevelopmentStage startStage) {
        this.startStage = startStage;
    }

    public void setEndStage(DevelopmentStage endStage) {
        this.endStage = endStage;
    }
}
