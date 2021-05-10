package org.zfin.ontology.datatransfer.service;

public class ExpressionResultUpdateRecord {
    private Long expressionResultID;
    private String startStageID;
    private String endStageID;
    private String superTermOboID;
    private String subTermOboID;

    String getEndStageID() {
        return endStageID;
    }

    void setEndStageID(String endStageID) {
        this.endStageID = endStageID;
    }

    Long getExpressionResultID() {
        return expressionResultID;
    }

    void setExpressionResultID(Long expressionResultID) {
        this.expressionResultID = expressionResultID;
    }

    String getStartStageID() {
        return startStageID;
    }

    void setStartStageID(String startStageID) {
        this.startStageID = startStageID;
    }

    String getSuperTermOboID() {
        return superTermOboID;
    }

    void setSuperTermOboID(String superTermOboID) {
        this.superTermOboID = superTermOboID;
    }

    public String getSubTermOboID() {
        return subTermOboID;
    }

    public void setSubTermOboID(String subTermOboID) {
        this.subTermOboID = subTermOboID;
    }
}
