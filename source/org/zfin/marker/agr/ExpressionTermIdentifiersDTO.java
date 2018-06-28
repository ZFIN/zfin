package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExpressionTermIdentifiersDTO {

    private String whereExpressionStatement;
    private String cellularComponentTermId;
    private String anatomicalStructureTermId;
    private String anatommicalSubStructureTermId;
    private String anatomicalStructureQualifierTermId;

    public ExpressionTermIdentifiersDTO(String whereExpressedStatement, String cellularComponentTermId,
                                        String anatomicalStructureTermId, String anatomicalStructureQualifierTermId, String anatommicalSubStructureTermId) {
        this.whereExpressionStatement = whereExpressedStatement;
        this.cellularComponentTermId = cellularComponentTermId;
        this.anatomicalStructureTermId = anatomicalStructureTermId;
        this.anatomicalStructureQualifierTermId = anatomicalStructureQualifierTermId;

    }
}
