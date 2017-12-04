package org.zfin.expression.presentation;

import org.zfin.anatomy.DevelopmentStage;

public class ExpressionSearchResult {

    private String id;
    private DevelopmentStage startStage;
    private DevelopmentStage endStage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DevelopmentStage getStartStage() {
        return startStage;
    }

    public void setStartStage(DevelopmentStage startStage) {
        this.startStage = startStage;
    }

    public DevelopmentStage getEndStage() {
        return endStage;
    }

    public void setEndStage(DevelopmentStage endStage) {
        this.endStage = endStage;
    }

}
