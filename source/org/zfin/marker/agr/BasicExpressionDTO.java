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
    private String whenExpressedStage;
    private CrossReferenceDTO crossReference;
    private String assay;
    private List<DataProviderDTO> dataProviderList;
    private ExpressionTermIdentifiersDTO wildtypeExpressionTermIdentifiers;

    public ExpressionTermIdentifiersDTO getWildtypeExpressionTermIdentifiers() {
        return wildtypeExpressionTermIdentifiers;
    }

    public void setWildtypeExpressionTermIdentifiers(ExpressionTermIdentifiersDTO wildtypeExpressionTermIdentifiers) {
        this.wildtypeExpressionTermIdentifiers = wildtypeExpressionTermIdentifiers;
    }

    public CrossReferenceDTO getCrossReference() {
        return crossReference;
    }

    public void setCrossReference(CrossReferenceDTO crossReference) {
        this.crossReference = crossReference;
    }

    public List<DataProviderDTO> getDataProviderList() {
        return dataProviderList;
    }

    public void setDataProviderList(List<DataProviderDTO> dataProviderList) {
        this.dataProviderList = dataProviderList;
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

    public String getWhenExpressedStage() { return whenExpressedStage; }

    public void setWhenExpressedStage(String whenExpressedStage) { this.whenExpressedStage = whenExpressedStage; }

    public String getAssay() { return assay; }

    public void setAssay(String assay) { this.assay = assay; }

    public GregorianCalendar getDateAssigned() { return dateAssigned; }

    public void setDateAssigned(GregorianCalendar dateAssigned) { this.dateAssigned = dateAssigned; }

}
