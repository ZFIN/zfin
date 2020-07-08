package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExpressionTermIdentifiersDTO {

    private String whereExpressedStatement;
    private String cellularComponentTermId;
    private String anatomicalStructureTermId;

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

    public void setAnatomicalSubStructureTermId(String anatomicalSubStructureTermId) {
        this.anatomicalSubStructureTermId = anatomicalSubStructureTermId;
    }

    public String getAnatomicalStructureQualifierTermId() {
        return anatomicalStructureQualifierTermId;
    }

    public void setAnatomicalStructureQualifierTermId(String anatomicalStructureQualifierTermId) {
        this.anatomicalStructureQualifierTermId = anatomicalStructureQualifierTermId;
    }

    public Set<UberonSlimTermDTO> getAnatomicalStructureUberonSlimTermIds() {
        return anatomicalStructureUberonSlimTermIds;
    }

    public void setAnatomicalStructureUberonSlimTermIds(Set<UberonSlimTermDTO> anatomicalStructureUberonSlimTermIds) {
        this.anatomicalStructureUberonSlimTermIds = anatomicalStructureUberonSlimTermIds;
    }

    private String anatomicalSubStructureTermId;
    private String anatomicalStructureQualifierTermId;
    private Set<UberonSlimTermDTO> anatomicalStructureUberonSlimTermIds;

    public ExpressionTermIdentifiersDTO(String whereExpressedStatement, String cellularComponentTermId,
                                        String anatomicalStructureTermId, String anatomicalSubStructureTermId,
                                        String anatomicalStructureQualifierTermId, Set<UberonSlimTermDTO> anatomicalStructureUberonSlimTermIds) {
        this.whereExpressedStatement = whereExpressedStatement;
        this.cellularComponentTermId = cellularComponentTermId;
        this.anatomicalStructureTermId = anatomicalStructureTermId;
        this.anatomicalStructureQualifierTermId = anatomicalStructureQualifierTermId;
        this.anatomicalSubStructureTermId = anatomicalSubStructureTermId;
        this.anatomicalStructureUberonSlimTermIds = anatomicalStructureUberonSlimTermIds;

    }

}
