package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExpressionTermIdentifiersDTO {

    private String whereExpressedStatement;
    private String cellularComponentTermId;
    private String anatomicalStructureTermId;
    private String anatomicalSubStructureTermId;
    private String anatomicalStructureQualifierTermId;
    private Set<UberonSlimTermDTO> anatomcialStructureUberonSlimTermIds;

    public String getWhereExpressedStatement() {
        return whereExpressedStatement;
    }

    public void setWhereExpressedStatement(String whereExpressedStatement) {
        this.whereExpressedStatement = whereExpressedStatement;
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

    public String getAnatomicalSubStructureTermId() {
        return anatomicalSubStructureTermId;
    }

    public void setAnatomicalSubStructureTermId(String anatommicalSubStructureTermId) {
        this.anatomicalSubStructureTermId = anatommicalSubStructureTermId;
    }

    public String getAnatomicalStructureQualifierTermId() {
        return anatomicalStructureQualifierTermId;
    }

    public void setAnatomicalStructureQualifierTermId(String anatomicalStructureQualifierTermId) {
        this.anatomicalStructureQualifierTermId = anatomicalStructureQualifierTermId;
    }

    public Set<UberonSlimTermDTO> getAnatomcialStructureUberonSlimTermIds() {
        return anatomcialStructureUberonSlimTermIds;
    }

    public void setAnatomcialStructureUberonSlimTermIds(Set<UberonSlimTermDTO> anatomcialStructureUberonSlimTermIds) {
        this.anatomcialStructureUberonSlimTermIds = anatomcialStructureUberonSlimTermIds;
    }

    public ExpressionTermIdentifiersDTO(String whereExpressedStatement, String cellularComponentTermId,
                                        String anatomicalStructureTermId, String anatomicalSubStructureTermId,
                                        String anatomicalStructureQualifierTermId, Set<UberonSlimTermDTO> anatomcialStructureUberonSlimTermIds) {
        this.whereExpressedStatement = whereExpressedStatement;
        this.cellularComponentTermId = cellularComponentTermId;
        this.anatomicalStructureTermId = anatomicalStructureTermId;
        this.anatomicalStructureQualifierTermId = anatomicalStructureQualifierTermId;
        this.anatomicalSubStructureTermId = anatomicalSubStructureTermId;
        this.anatomcialStructureUberonSlimTermIds = anatomcialStructureUberonSlimTermIds;

    }
}
