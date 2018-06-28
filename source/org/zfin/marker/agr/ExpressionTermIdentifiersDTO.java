package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExpressionTermIdentifiersDTO {

    private String whereExpressionStatement;
    private String cellularComponentTermId;
    private String anatomicalStructureTermId;
    private String anatommicalSubStructureTermId;
    private String anatomicalStructureQualifierTermId;

    public String getWhereExpressionStatement() {
        return whereExpressionStatement;
    }

    public void setWhereExpressionStatement(String whereExpressionStatement) {
        this.whereExpressionStatement = whereExpressionStatement;
    }

    public String getCellularComponentTermId() {
        return cellularComponentTermId;
    }

    public void setCellularComponentTermId(String cellularComponentTermId) {
        this.cellularComponentTermId = cellularComponentTermId;
    }

    public String getAnatomicalStructureTermId() {
        return anatomicalStructureTermId;
    }

    public void setAnatomicalStructureTermId(String anatomicalStructureTermId) {
        this.anatomicalStructureTermId = anatomicalStructureTermId;
    }

    public String getAnatommicalSubStructureTermId() {
        return anatommicalSubStructureTermId;
    }

    public void setAnatommicalSubStructureTermId(String anatommicalSubStructureTermId) {
        this.anatommicalSubStructureTermId = anatommicalSubStructureTermId;
    }

    public String getAnatomicalStructureQualifierTermId() {
        return anatomicalStructureQualifierTermId;
    }

    public void setAnatomicalStructureQualifierTermId(String anatomicalStructureQualifierTermId) {
        this.anatomicalStructureQualifierTermId = anatomicalStructureQualifierTermId;
    }


    public ExpressionTermIdentifiersDTO(String whereExpressedStatement, String cellularComponentTermId,
                                        String anatomicalStructureTermId, String anatomicalStructureQualifierTermId, String anatommicalSubStructureTermId) {
        this.whereExpressionStatement = whereExpressedStatement;
        this.cellularComponentTermId = cellularComponentTermId;
        this.anatomicalStructureTermId = anatomicalStructureTermId;
        this.anatomicalStructureQualifierTermId = anatomicalStructureQualifierTermId;
        this.anatommicalSubStructureTermId = anatommicalSubStructureTermId;

    }
}
