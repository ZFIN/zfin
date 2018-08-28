package org.zfin.marker.agr;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.zfin.util.JsonDateSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.GregorianCalendar;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BasicExpressionDTO {

    @JsonSerialize(using = JsonDateSerializer.class)
    private GregorianCalendar dateAssigned = new GregorianCalendar();

    private String geneId;
    private PublicationAgrDTO evidence;
    private CrossReferenceDTO crossReference;
    private String assay;
    private DataProviderDTO dataProvider;
    private ExpressionStageIdentifiersDTO whenExpressed;
    private ExpressionTermIdentifiersDTO whereExpressed;

    public ExpressionStageIdentifiersDTO getWhenExpressed() {
        return whenExpressed;
    }

    public void setWhenExpressed(ExpressionStageIdentifiersDTO whenExpressed) {
        this.whenExpressed = whenExpressed;
    }

    public ExpressionTermIdentifiersDTO getWhereExpressed() {
        return whereExpressed;
    }

    public void setWhereExpressed(ExpressionTermIdentifiersDTO whereExpressed) {
        this.whereExpressed = whereExpressed;
    }

    public CrossReferenceDTO getCrossReference() {
        return crossReference;
    }

    public void setCrossReference(CrossReferenceDTO crossReference) {
        this.crossReference = crossReference;
    }

    public DataProviderDTO getDataProvider() {
        return dataProvider;
    }

    public void setDataProviderList(DataProviderDTO dataProvider) {
        this.dataProvider = dataProvider;
    }

    public String getGeneId() {
        return geneId;
    }

    public void setGeneId(String geneId) {
        this.geneId = geneId;
    }

    public PublicationAgrDTO getEvidence() {
        return evidence;
    }

    public void setEvidence(PublicationAgrDTO evidence) {
        this.evidence = evidence;
    }

    public String getAssay() { return assay; }

    public void setAssay(String assay) { this.assay = assay; }

    public GregorianCalendar getDateAssigned() { return dateAssigned; }

    public void setDateAssigned(GregorianCalendar dateAssigned) { this.dateAssigned = dateAssigned; }

}
